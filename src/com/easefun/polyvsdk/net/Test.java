package com.easefun.polyvsdk.net;

public class Test {

	public static void main(String args[]) {
		PolyvUploadClient client = new PolyvUploadClient("userid","readtoken","writetoken","我的标题","polyvSDK",1);
		client.setFilename("F:\\文档\\v\\13993026.mp4");
		client.setProgress(new ProgressImpl());
		client.upload();
		System.out.println(client.getLocation());
		System.out.println(client.getJson());

	}
}
