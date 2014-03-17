package com.vst.itv52.v1.biz;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.VideoDetailInfo;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoSet;
import com.vst.itv52.v1.model.VideoSource;

public class VideoDetailBiz {

	/**
	 * http://so.52itv.cn/?data=info&id=@id
	 * 
	 * @param id
	 * @return
	 */
	public static VideoDetailInfo parseDetailInfo(String url, int id,
			int verCode) {
		url = url + id + ".json";
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
//		System.out.println(json);
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(json);
			JsonNode playlist = rootNode.path("playlist");
			for (JsonNode playlistNode : playlist) {
				for (JsonNode listNode : playlistNode.path("list")) {
					mapper.treeToValue(listNode, VideoSet.class);
				}
				mapper.treeToValue(playlistNode, VideoSource.class);
			}
			JsonNode new_top = rootNode.path("new_top");
			for (JsonNode jsonNode : new_top) {
				mapper.treeToValue(jsonNode, VideoInfo.class);
			}
			VideoDetailInfo detail = mapper.treeToValue(rootNode,
					VideoDetailInfo.class);
			return detail;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
