package com.vst.itv52.v1.biz;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.ChannelColumnBean;
import com.vst.itv52.v1.model.ChannelColumns;
import com.vst.itv52.v1.model.TVBackChannelJavabean;

public class TvBackBiz {

	/**
	 * 解析频道列表
	 * 
	 * @param url
	 * @return
	 */
	public static TVBackChannelJavabean praseChannelList(String url) {
		// url = "http://v.52itv.cn/vst_cn/tvback.php";
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
//		System.out.println(json);

		TVBackChannelJavabean channelbean = new TVBackChannelJavabean();
		// ObjectMapper mapper = new ObjectMapper();
		// JsonNode rootNode = mapper.readTree(json);
		// JsonNode type = rootNode.path("datalist");

		try {
			JSONObject object = new JSONObject(json);
			JSONArray date = object.getJSONArray("datelist");
			Map<String, String> dateMap = new LinkedHashMap<String, String>();
			for (int i = 0; i < date.length(); i++) {
				JSONObject object2 = (JSONObject) date.opt(i);
				dateMap.put(object2.getString("name"),
						object2.getString("date"));
			}
			channelbean.setDateMap(dateMap);
			JSONArray channel = object.getJSONArray("tvback");
			Map<String, String> tvMap = new LinkedHashMap<String, String>();
			for (int j = 0; j < channel.length(); j++) {
				JSONObject object3 = (JSONObject) channel.opt(j);
				tvMap.put(object3.getString("vid"),
						object3.getString("channel"));
			}
			channelbean.setTvMap(tvMap);
			channelbean.setNexturl(object.getString("nexturl"));

			return channelbean;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析栏目列表
	 * 
	 * @param url
	 * @param vid
	 * @param date
	 * @return
	 */
	public static ChannelColumns parseColumns(String url,
			Map<String, String> map) {
		// String url = "http://v.52itv.cn/vst_cn/tvback.php";
		// NameValuePair[] pairs = new NameValuePair[2];
		// pairs[0] = new BasicNameValuePair("vid", vid);
		// pairs[1] = new BasicNameValuePair("date", date);
//		url += ".json";
//		System.out.println("back url = "+ url);
		NameValuePair[] pairs = HttpClientHelper.mapToPairs(map);
		String json = HttpUtils.getContent(url, null, pairs);
		if (json == null) {
			return null;
		}
//		System.out.println(json);
		try {
			ChannelColumns columns = new ChannelColumns();
			JSONObject object = new JSONObject(json);
			columns.setTitle(object.getString("title"));
			columns.setPlaydate(object.getString("playdate"));
			columns.setLiveurl(object.getString("liveurl"));
			JSONArray columnList = object.getJSONArray("tvback");
			ArrayList<ChannelColumnBean> columnBeans = new ArrayList<ChannelColumnBean>();
			for (int i = 0; i < columnList.length(); i++) {
				ChannelColumnBean bean = new ChannelColumnBean();
				JSONObject object2 = (JSONObject) columnList.opt(i);
				bean.setTime(object2.getString("time"));
				bean.setChannelText(object2.getString("name"));
				bean.setUrl(object2.getString("link"));
				columnBeans.add(bean);
			}
			columns.setList(columnBeans);
			columns.setNexturl(object.getString("playurl"));
			return columns;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList parseMP4url(String url) {
		// url = "http://v.52itv.cn/vst_cn/tvback.php";
		// NameValuePair[] pairs = new NameValuePair[1];
		// pairs[0] = new BasicNameValuePair("link", null);
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
//		System.out.println(json);
		try {
			JSONObject object = new JSONObject(json);
			JSONArray playlist = object.getJSONArray("play");
			Uri[] mp4Uri = new Uri[playlist.length()];
			int[] mp4Dur = new int[playlist.length()];
			for (int i = 0; i < playlist.length(); i++) {
				JSONObject playlink = (JSONObject) playlist.opt(i);
				mp4Dur[i] = Integer.parseInt(playlink.getString("dur"));
				mp4Uri[i] = Uri.parse(playlink.getString("url"));
			}
			ArrayList list = new ArrayList();
			list.add(mp4Uri);
			list.add(mp4Dur);
			return list;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
