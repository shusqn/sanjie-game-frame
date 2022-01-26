package com.frame.handler;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.frame.constant.RedisKey;
import com.frame.dao.RedisDao;
import com.frame.model.ServerInfo;
import com.google.gson.Gson;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseRedisServerHandler{

	private String serverKey;
	private long serverId;

	private static Map<Integer, ServerInfo> serversMap = new ConcurrentHashMap<Integer, ServerInfo>();
	/**
	 * @param sid
	 * @return
	 */
	public ServerInfo getServer(int sid) {
		return serversMap.get(sid);
	}
	
	/**
	 * @return
	 */
	public Collection<ServerInfo> getAllServer() {
		return serversMap.values();
	}
	
	/**
	 * @param serverType
	 * @return
	 */
	public ServerInfo getMinUsersServer(int serverType) {
		ServerInfo minInfo = null;
		for (Entry<Integer, ServerInfo> entry : serversMap.entrySet()) {
			ServerInfo info = entry.getValue();
			if(info.getStype() == serverType) {
				if(minInfo == null || minInfo.getOnlineUsers() > info.getOnlineUsers()) {
					minInfo = info;
				}
			}
		}
		return minInfo;
	}

	/**
	 * 本服务器的所有serverIdMap
	 */
	@Getter
	private static Map<String, BaseRedisServerHandler> localServersMap = new ConcurrentHashMap<String, BaseRedisServerHandler>();

	public void destroy() {
		try {
			RedisDao.getInstance().getTemplate().opsForHash().delete(RedisKey.SERVER_MAP, localServersMap.keySet().toArray());
			log.info("服务器关闭 server:{} 销毁", localServersMap.keySet().toString());
		} catch (Exception e) {
		}
	}

	/**
	 * 将服务器信息注册到redis
	 * @param serverId
	 * @param serverType
	 * @param ip
	 * @param port
	 * @param version
	 */
	public synchronized void register2Redis(int serverId, int serverType ){
		register2Redis(serverId, serverType, null, 0);
	}
	
	private  static boolean init = false;
	/**
	 * 将服务器信息注册到redis
	 * @param serverId
	 * @param serverType
	 * @param ip
	 * @param port
	 * @param version
	 */
	public synchronized void register2Redis(int serverId, int serverType, String hostname, int port){
		if(this.serverKey != null) {
			log.error("{} 服务器已经被注册", serverKey);
			return;
		}

		serverKey = RedisKey.SERVER +serverId;
		this.serverId = serverId;
		if(isServerStarted()) {
			log.error("serverKey:{} 服务器正在运行，请先关闭再启动", serverKey);
			destroy();
			System.exit(0);
			return;
		}

		Map<String, String> serverMap = new ConcurrentHashMap<String, String>(8);
		serverMap.put(RedisKey.SERVER_SID, serverId+"");
		serverMap.put(RedisKey.SERVER_STYPE, serverType+"");
		serverMap.put(RedisKey.SERVER_ONLINEUSERS, 0+"");
		if(hostname != null) {
			serverMap.put(RedisKey.SERVER_HOSTNAME, hostname);
			serverMap.put(RedisKey.SERVER_PORT, port+"");
		}
		RedisDao.getInstance().getTemplate().opsForHash().putAll(serverKey, serverMap);
		localServersMap.put(serverId + "", this);

		this.updateRunningTime();
		this.getGameServers();
		if(!init) {
			init = true;
			RedisDao.getRedisExecutor().scheduleAtFixedRate(()->{
				this.updateRunningTime();
			}, 0, 60, TimeUnit.SECONDS);
			
			RedisDao.getRedisExecutor().scheduleAtFixedRate(()->{
				this.getGameServers();
			}, 0, 60, TimeUnit.SECONDS);
		}
		log.info("server:{} 注册成功 data:{}",serverKey, serverMap.toString());
	}
	
	

	/**
	 * 更新服务器在线人数
	 * @param onlineUsers
	 */
	public void updateOnlineUsers(int onlineUsers){
		RedisDao.getInstance().getTemplate().opsForHash().put(serverKey, "onlineUsers", onlineUsers+"");
	}

	/**
	 * 服务器是否启动
	 */
	private boolean isServerStarted(){
		try {
			String uptime = (String) RedisDao.getInstance().getTemplate().opsForHash().get(RedisKey.SERVER_MAP, serverId +"");
			if(uptime != null && Long.valueOf(uptime) + TimeUnit.SECONDS.toMillis(60 * 2) > System.currentTimeMillis() ) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 获取token
	 * @param pid
	 * @param robot
	 * @return
	 */
	public String getToken(long pid, boolean robot){
		if(!robot) {
			return (String) RedisDao.getInstance().getTemplate().opsForHash().get(RedisKey.USER_MAP+pid, RedisKey.USER_MAP_TOKEN);
		}
		return (String) RedisDao.getInstance().getTemplate().opsForHash().get(RedisKey.USER_MAP+pid, RedisKey.ROBOT_MAP_TOKEN);
	}

	/**
	 * @param pid
	 * @param token
	 */
	public void setToken(long pid, String token){
		RedisDao.getInstance().getTemplate().opsForHash().put(RedisKey.USER_MAP+pid, RedisKey.USER_MAP_TOKEN, token);
	}
	
	/**
	 * @param pid
	 * @param token
	 */
	public void setRobotToken(long pid, String token){
		RedisDao.getInstance().getTemplate().opsForHash().put(RedisKey.USER_MAP+pid, RedisKey.ROBOT_MAP_TOKEN, token);
	}
	
	/**
	 * @param pid
	 */
	public void delToken(long pid){
		RedisDao.getInstance().getTemplate().opsForHash().delete(RedisKey.USER_MAP+pid, RedisKey.USER_MAP_TOKEN);
	}

	private void updateRunningTime(){
		try {
			Map<String, String> serverMap = new ConcurrentHashMap<String, String>();
			
			String utime = System.currentTimeMillis() + "";
			for (String localServerId : localServersMap.keySet()) {
				serverMap.put(localServerId, utime);
			}
			RedisDao.getInstance().getTemplate().opsForHash().putAll(RedisKey.SERVER_MAP, serverMap);
			log.info("up time {} {}", serverMap.toString(), utime);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private Gson gson = new Gson();
	/**
	 * 获取服务器列表
	 */
	public void getGameServers() {
		try {
			Map<Object, Object> serversOnlineMap = RedisDao.getInstance().getTemplate().opsForHash().entries(RedisKey.SERVER_MAP);
			for (Entry<Object, Object> entry : serversOnlineMap.entrySet()) {
				int sid = Integer.valueOf(entry.getKey().toString());
				long uptime = Long.valueOf(entry.getValue().toString());
				if(Long.valueOf(uptime) < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60 * 3)) {
					serversMap.remove(sid);
					continue;
				}

				Map<Object, Object> serverInfo= RedisDao.getInstance().getTemplate().opsForHash().entries(RedisKey.SERVER + sid);
				serversMap.put(sid, gson.fromJson(gson.toJson(serverInfo), ServerInfo.class));
			}

			log.info("serverId:{} {}", serverId, gson.toJson(serversMap));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @param pid
	 * @param gameServerId
	 */
	public void setUserGameServerId(long pid, int gameServerId) {
		RedisDao.getInstance().getTemplate().opsForHash().put(RedisKey.USER_MAP + pid, RedisKey.USER_MAP_GAME_SID, gameServerId + "");
	}

	/**
	 * @param pid
	 */
	public int getUserGameServerId(long pid) {
		Object gameServerId = RedisDao.getInstance().getTemplate().opsForHash().get(RedisKey.USER_MAP + pid, RedisKey.USER_MAP_GAME_SID);
		if(gameServerId == null) {
			return 0;
		}
		return Integer.valueOf(gameServerId.toString());
	}

	/**
	 * @param pid
	 */
	public void removeUserGameServerId(long pid) {
		RedisDao.getInstance().getTemplate().opsForHash().delete(RedisKey.USER_MAP +pid, RedisKey.USER_MAP_GAME_SID);
	}

}
