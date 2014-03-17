package com.vst.itv52.v1.biz;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.LiveDataInfo;

public class LiveBiz {
	private static final String TAG = "LiveBiz";
	
	public static final String LIVEUPDAT_ACTION = "com.vst.itv52.v1.liveupdate_ACION";

	public static Bundle getLiveEPG(Context context, String epgid)
			throws Exception {
		/* 解析内置EPG信息 */
		if (MyApp.LiveSeek != "0") {
			Bundle data = new Bundle();
			data.putInt("progress",
					getLiveProgress(MyApp.LiveEpg, MyApp.LiveNextEpg));
			data.putString("dqjm", MyApp.LiveEpg);
			data.putString("xgjm", MyApp.LiveNextEpg);
			Log.d(TAG, "解析内置EPG信息成功！！！  -> " + MyApp.LiveEpg);
			return data;
		}
		/* 解析服务器接口EPG */
		if (!HttpUtils.isNetworkAvailable(context)) {
			throw new Exception("not net");
		}
		if (epgid.length() < 3)
			return null;
		try {
			String url = "http://so.52itv.cn/vst_cn/tvepg.php?open=xml&epgid="+ epgid;
			String xml = HttpUtils.getContent(url, null, null);
			SAXReader reader = new SAXReader();
			Document document = reader.read(new ByteArrayInputStream(xml
					.getBytes("utf-8")));
			Element root = document.getRootElement();
			Bundle data = new Bundle();
			String dqjm_Name = root.element("dqjm").attributeValue("name");
			String xgjm_Name = root.element("xgjm").attributeValue("name");
			data.putInt("progress", getLiveProgress(dqjm_Name, xgjm_Name));
			data.putString("dqjm", dqjm_Name);
			data.putString("xgjm", xgjm_Name);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/* 计算EPG播放进度 */
	public static int getLiveProgress(String startEpg, String endEpg) {
		long BF_Time = 45;
		System.out.println("Start=" + startEpg + ",end=" + endEpg);
		if (endEpg != null && startEpg != null && endEpg != "-"
				&& startEpg != "-") {
			String End_Time = endEpg.substring(0, 5).trim();
			String Start_Time = startEpg.substring(0, 5).trim();
			if (Start_Time.length() == 5 && End_Time.length() == 5
					&& Start_Time.contains(":") && End_Time.contains(":")) {
				long Z_Time = minDiff(Start_Time, End_Time);
				Log.d(TAG, "Z_Time = " + Z_Time);
				long X_Time = minDiff(getStringDateHM(), End_Time);
				Log.d(TAG, "X_Time = " + X_Time);
				if (X_Time < X_Time) {
					BF_Time = 100;
				} else {
					BF_Time = (Z_Time - X_Time) * 100 / Z_Time;
				}
			}
		}
		Log.d(TAG, "播放进度 " + BF_Time + "%");
		return (int) BF_Time;
	}

	public static long minDiff(String startTime, String endTime) {
		return (strtotime(endTime) - strtotime(startTime)) / 60;
	}

	@SuppressLint("SimpleDateFormat")
	public static long strtotime(String time) {
		long unixTimestamp = 0;
		try {
			Date date = new SimpleDateFormat("HH:mm").parse(time);
			unixTimestamp = date.getTime() / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return unixTimestamp;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getStringDateHM() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	public static LiveDataInfo parseLiveData(String baseurl) {
		String netDefineLive = "http://fav.91vst.com/live_json.asp";
		try {
			if (baseurl == null) {
				return null;
			}
			if (MyApp.getChanState() == 2) {
				baseurl = netDefineLive;
			}
//			System.out.println("请求直播：" + baseurl);
			String json = HttpUtils.getContent(baseurl, null, null);
//			System.out.println(json);
			ObjectMapper mapper = new ObjectMapper();
			LiveDataInfo data = mapper.readValue(json, LiveDataInfo.class);
			return data;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
