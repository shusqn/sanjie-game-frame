package com.frame.model.mq;

import com.frame.mobel.WaitProtocol;
import com.frame.mongodto.BaseCollection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PVERoomState extends BaseCollection{
	private long roundId;
	private int state;
	private WaitProtocol waitProtocol;
}
