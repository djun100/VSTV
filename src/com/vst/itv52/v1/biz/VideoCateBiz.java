package com.vst.itv52.v1.biz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;

import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoList;
import com.vst.itv52.v1.model.VideoTypeInfo;

public class VideoCateBiz {

	public static ArrayList<VideoTypeInfo> parseTopCate(Context context,
			String url, boolean fromCache) {
		url = url + "item";
//		System.out.println("分类页" + url);
		ArrayList<VideoTypeInfo> list = null;
		/**
		 * 从文件获取
		 */
		File cateFile = new File(context.getCacheDir(), "cateFile");
		if (fromCache) {
			if (cateFile.exists()) {
				try {
					list = new ArrayList<VideoTypeInfo>();
					ObjectMapper mapper = new ObjectMapper();
					JsonNode rootNode = mapper.readTree(cateFile);
					JsonNode type = rootNode.path("type");
					for (JsonNode jsonNode : type) {
						VideoTypeInfo typeInfo = mapper.treeToValue(jsonNode,
								VideoTypeInfo.class);
						System.out.println(typeInfo);
						list.add(typeInfo);
					}
					// 部分型号电影、电视剧排序颠倒，原因不明，此处换回来
					if (!list.isEmpty() && list.get(1).name.equals("电影")) {
						VideoTypeInfo infoTemp = list.remove(1);
						list.add(2, infoTemp);
					}
					return list;
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String json = HttpUtils.getContent(url, null, null);
		if (json != null) {
//			System.out.println("分类页数据：" + json);
			try {
				// 保存文件
				FileOutputStream fos = new FileOutputStream(cateFile);
				ByteArrayInputStream bis = new ByteArrayInputStream(
						json.getBytes());
				int count = -1;
				byte[] buffer = new byte[2048];
				while ((count = bis.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				bis.close();

				list = new ArrayList<VideoTypeInfo>();
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(json);
				JsonNode type = rootNode.path("type");
				for (JsonNode jsonNode : type) {
					VideoTypeInfo typeInfo = mapper.treeToValue(jsonNode,
							VideoTypeInfo.class);
					System.out.println(typeInfo);
					list.add(typeInfo);
				}
				// 部分型号电影、电视剧排序颠倒，原因不明，此处换回来
				if (!list.isEmpty() && list.get(1).name.equals("电影")) {
					VideoTypeInfo infoTemp = list.remove(1);
					list.add(2, infoTemp);
				}
				return list;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static VideoList parseVideoList(String url,
			Map<String, String> parames) {
		if (parames.get("item") != null
				&& parames.get("item").equalsIgnoreCase("全部"))
			parames.remove("item");
		if (parames.get("area") != null
				&& parames.get("area").equalsIgnoreCase("不限"))
			parames.remove("area");
		NameValuePair[] pairs = HttpClientHelper.mapToPairs(parames);
		// System.out.println("pairs parames =" + pairs);
		String json = HttpUtils.getContent(url, null, pairs);
		if (json == null) {
			return null;
		}
		// System.out.println(json);
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(json);
			JsonNode videoNode = rootNode.path("video");
			for (JsonNode jsonNode : videoNode) {
				mapper.treeToValue(jsonNode, VideoInfo.class);
			}
			return mapper.treeToValue(rootNode, VideoList.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, ArrayList<String>> parseCateList(String url,
			String tid) {
		String json = HttpUtils.getContent(url, null, new NameValuePair[] {
				new BasicNameValuePair("tid", tid),
				new BasicNameValuePair("data", "item") });
		try {
			Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(json);
			JsonNode itemNode = rootNode.path("item");
			ArrayList<String> itemList = new ArrayList<String>();
			for (JsonNode jsonNode : itemNode) {
				itemList.add(jsonNode.path("name").asText());
			}

			JsonNode areaNode = rootNode.path("area");
			ArrayList<String> areaList = new ArrayList<String>();
			for (JsonNode jsonNode : areaNode) {
				areaList.add(jsonNode.path("name").asText());
			}

			JsonNode yearNode = rootNode.path("year");
			ArrayList<String> yearList = new ArrayList<String>();
			for (JsonNode jsonNode : yearNode) {
				yearList.add(jsonNode.path("name").asText());
			}

			map.put("item", itemList);
			map.put("area", areaList);
			map.put("year", yearList);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
