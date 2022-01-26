package com.frame.model.game;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Banker {
	/**id**/
	private long roomId;

	/**庄家剩余轮数**/
	private int bankerRoundFree;
	/**局号**/
	private long roundId = 0;

	private int bankerType;
	/**上庄id**/
	private long bankerGroupId;
	/**庄家余额**/
	private long bankerBalance;
	/**上庄总轮数**/
	private int totalRound;
	
	/**当局在庄上的庄家信息**/
	private List<OnBanker> onbankerUsers = new ArrayList<OnBanker>();
	/**是否所有机器人上庄**/
	private boolean isAllRobotOnBanker;
	/**当局输赢情况**/
	private long bankerWinlost;

}
