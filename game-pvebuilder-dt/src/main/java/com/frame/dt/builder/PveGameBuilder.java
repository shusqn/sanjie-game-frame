package com.frame.dt.builder;

import java.util.ArrayList;
import java.util.List;

import com.frame.constant.RedisKey;
import com.frame.dao.RedisDao;
import com.frame.dt.DtMrg;
import com.frame.dt.rocketmq.RocketMqSender;
import com.frame.entity.Room;
import com.frame.executor.RoomExecutorHandler;
import com.frame.mobel.WaitProtocol;
import com.frame.mobel.card.Card;
import com.frame.mobel.card.PlayerType;
import com.frame.mobel.card.game.DragonTigerCardsUtils;
import com.frame.model.mq.GameCardsResult;
import com.frame.model.mq.GamePlayerCards;
import com.frame.model.mq.GameReslut;
import com.frame.model.mq.PVERoomState;
import com.frame.protobuf.PveGameMsg;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PveGameBuilder {
	private long roundId = 0;
	private List<Card> cardsRepository = new ArrayList<Card>();
	private final Room config;
	private int state;
	private WaitProtocol waitProtocol;
	private GamePlayerCards gamePlayerCards = new GamePlayerCards();
	private GameReslut gameReslut = new GameReslut();
	private GameCardsResult gameCardsResult = new GameCardsResult();
	private PVERoomState pveRoomState = new PVERoomState();
	
	private List<Card> dragonCards = new ArrayList<Card>();
	private List<Card> tigerCards = new ArrayList<Card>();
	
	
	private static  Gson gson = new Gson();
	
	public PveGameBuilder(Room config) {
		this.config = config;
		gamePlayerCards.getPlayerCards().put(PlayerType.DRAGON.getValue(), dragonCards);
		gamePlayerCards.getPlayerCards().put(PlayerType.TIGER.getValue(), tigerCards);
		start();
	}
	
	/**
	 * 
	 */
	private void start() {
		this.roundId = DtMrg.getInsance().getIdBuilder().nextId();
		this.state = PveGameMsg.DeskState.START_COUNTDOWN_VALUE;
		waitProtocol = WaitProtocol.build(5 * 1000);
		RoomExecutorHandler.getInstance().executeDelay(waitProtocol.getDisMills(), this.config.getRoomId(), this::bet, "bet");
		
		
		RedisDao.getInstance().getTemplate().delete(RedisKey.ROOM_MAP_KEY + this.config.getRoomId());
		
		sendRoomState();
	}
	
	/**
	 * 
	 */
	private void bet() {
		this.state = PveGameMsg.DeskState.BET_VALUE;
		waitProtocol = WaitProtocol.build(16 * 1000);
		RoomExecutorHandler.getInstance().executeDelay(waitProtocol.getDisMills(), this.config.getRoomId(), this::showCards, "showCards");
		sendRoomState();
	}
	
	/**
	 * 
	 */
	private void showCards() {
		this.state = PveGameMsg.DeskState.SHOW_CARDS_VALUE;
		waitProtocol = WaitProtocol.build(6 * 1000);
		RoomExecutorHandler.getInstance().executeDelay(waitProtocol.getDisMills(), this.config.getRoomId(), this::showGameCardsResult, "showGameCardsResult");
		sendRoomState();
		
		if(cardsRepository.size() <= 7) {
			//洗牌
			cardsRepository.clear();
			cardsRepository.addAll(DragonTigerCardsUtils.shuffleDtCards());
		}
		dragonCards.clear();
		tigerCards.clear();
		dragonCards.add(cardsRepository.remove(0));
		tigerCards.add(cardsRepository.remove(0));
		
		//
		RocketMqSender.getInstance().noticePveGames(gamePlayerCards, this.config.getRoomId());
		RedisDao.getInstance().getTemplate().opsForHash().put(RedisKey.ROOM_MAP_KEY + this.config.getRoomId(), gamePlayerCards.getClass().getSimpleName(), gson.toJson(gamePlayerCards));
	}
	
	/**
	 * 
	 */
	private void  showGameCardsResult() {
		this.state = PveGameMsg.DeskState.SHOW_CARDS_RESULT_VALUE;
		waitProtocol = WaitProtocol.build(6 * 1000);
		RoomExecutorHandler.getInstance().executeDelay(waitProtocol.getDisMills(), this.config.getRoomId(), this::showGameResult, "showGameResult");
		sendRoomState();
		
		gameCardsResult.getCardsResult().clear();
		gameCardsResult.getCardsResult().put(PlayerType.DRAGON.getValue(), DragonTigerCardsUtils.getCardsResult(dragonCards.get(0)));
		gameCardsResult.getCardsResult().put(PlayerType.TIGER.getValue(), DragonTigerCardsUtils.getCardsResult(tigerCards.get(0)));
		//
		RocketMqSender.getInstance().noticePveGames(gameCardsResult, this.config.getRoomId());
		
		RedisDao.getInstance().getTemplate().opsForHash().put(RedisKey.ROOM_MAP_KEY + this.config.getRoomId(), gameCardsResult.getClass().getSimpleName(), gson.toJson(gameCardsResult));
	}
	
	/**
	 * 
	 */
	private void  showGameResult() {
		this.state = PveGameMsg.DeskState.SHOW_GAME_RESULT_VALUE;
		waitProtocol = WaitProtocol.build(6 * 1000);
		RoomExecutorHandler.getInstance().executeDelay(waitProtocol.getDisMills(), this.config.getRoomId(), this::settlement, "settlement");
		sendRoomState();
		
		gameReslut.getResluts().clear();
		gameReslut.getResluts().add(DragonTigerCardsUtils.getCardsResult(
				gameCardsResult.getCardsResult().get(PlayerType.DRAGON.getValue()),
				gameCardsResult.getCardsResult().get(PlayerType.TIGER.getValue()))
				);
		//
		RocketMqSender.getInstance().noticePveGames(gameReslut, this.config.getRoomId());
		
		RedisDao.getInstance().getTemplate().opsForHash().put(RedisKey.ROOM_MAP_KEY + this.config.getRoomId(), gameReslut.getClass().getSimpleName(), gson.toJson(gameReslut));
	}
	
	/**
	 * 
	 */
	private void  settlement() {
		this.state = PveGameMsg.DeskState.CALCULATING_VALUE;
		waitProtocol = WaitProtocol.build(6 * 1000);
		RoomExecutorHandler.getInstance().executeDelay(waitProtocol.getDisMills(), this.config.getRoomId(), this::start, "start");
		sendRoomState();
	}
	
	/**
	 * 
	 */
	private void sendRoomState() {
		pveRoomState.setState(this.state);
		pveRoomState.setWaitProtocol(this.waitProtocol);
		pveRoomState.setRoundId(this.roundId);
		RocketMqSender.getInstance().noticePveGames(pveRoomState, this.config.getRoomId());
		
		RedisDao.getInstance().getTemplate().opsForHash().put(RedisKey.ROOM_MAP_KEY + this.config.getRoomId(), pveRoomState.getClass().getSimpleName(), gson.toJson(pveRoomState));
	}
}
