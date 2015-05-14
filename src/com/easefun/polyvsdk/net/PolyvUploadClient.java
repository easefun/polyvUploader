package com.easefun.polyvsdk.net;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class PolyvUploadClient {

	private static final String api_url = "http://v.polyv.net:1080/files/";
	private static HttpClient httpClient = HttpClientBuilder.create().build();
	private String userid = null;
	private String readToken = null;
	private String writeToken = null;
	private String filename = null;
	private String vid = null;
	private String location = null;
	private int offset = 0;
	private Progress progress = null;
	private String json = null;
	private String title;
	private String desc;
	private long cataid;
	private String ext; // 拓展名

	public PolyvUploadClient(String userid, String readToken, String writeToken,String title,String desc,long cataid) {
		this.userid = userid;
		this.readToken = readToken;
		this.writeToken = writeToken;
		this.title=title;
		this.desc=desc;
		this.cataid=cataid;
	}

	public void setFilename(String filename) {
		String md5 = getMD5(filename);
		this.filename = filename;
		this.ext = filename.substring(filename.lastIndexOf(".")+1);
		this.vid = this.userid + md5.substring(10) + "_"
				+ this.userid.substring(0, 1);
		this.location = api_url + this.vid;
	}

	public void setProgress(Progress progress) {
		this.progress = progress;
	}

	public String upload() {
		try {
			int offset = this.offset();
			if (offset == -1) {
				this.create();
				offset = 0;
			}
			this.transfer(offset);
			return this.getJson();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//
	public void create() throws Exception {
		long filesize = new File(this.filename).length();

		HttpPost http = new HttpPost(api_url);
		http.addHeader("Final-Length", String.valueOf(filesize));
		http.addHeader("writeToken", this.writeToken);
		http.addHeader("vid", this.vid);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("title",title));
        params.add(new BasicNameValuePair("desc",desc));
        params.add(new BasicNameValuePair("cataid",Long.toString(cataid)));
        params.add(new BasicNameValuePair("ext",ext));
        http.setEntity(new  UrlEncodedFormEntity(params, HTTP.UTF_8));
		HttpResponse response = this.httpClient.execute(http);
		Header[] headers = response.getHeaders("Location");
		this.location = headers[0].getValue();
	}

	//
	public int offset() throws Exception {
		try {
			HttpHead http = new HttpHead(this.location);
			HttpResponse response = this.httpClient.execute(http);
			Header[] headers = response.getHeaders("Offset");
			return Integer.valueOf(headers[0].getValue());
		} catch (Exception e) {
			return -1;
		}
	}

	//
	public void transfer(int offset) throws Exception {
		long filesize = new File(this.filename).length();
		FileInputStream istream = new FileInputStream(new File(filename));

		HttpPatch http = new HttpPatch(this.location);
		http.addHeader("writeToken", this.writeToken);
		http.addHeader("Offset", String.valueOf(offset));

		http.setEntity(new IStreamEntity(istream, filesize, offset,
				this.progress));
		// http.setEntity(new InputStreamEntity(istream));
		HttpResponse response = this.httpClient.execute(http);
		HttpEntity entity = response.getEntity();
		this.json = EntityUtils.toString(entity);
	}

	private static String getMD5(String filename) {
		String checksum = null;
		try {
			checksum = new String(Hex.encodeHex(DigestUtils.md5(new FileInputStream(filename))));
			/*File file = new File(filename);
			FileInputStream in = new FileInputStream(file);
			MappedByteBuffer byteBuffer = in.getChannel().map(
					FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			return bi.toString(16);*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return checksum;
	}

	public String getLocation() {
		return this.location;
	}

	public String getReadToken() {
		return this.readToken;
	}

	public String getWriteToken() {
		return this.writeToken;
	}

	public String getJson() {
		return this.json;
	}
}
