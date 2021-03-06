CREATE DATABASE `game` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;


-- game.account definition

CREATE TABLE `account` (
  `uid` bigint(20) NOT NULL,
  `balance` bigint(20) NOT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- game.account_history definition

CREATE TABLE `account_history` (
  `id` bigint(20) NOT NULL,
  `balance` bigint(20) NOT NULL,
  `billId` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- game.bill definition

CREATE TABLE `bill` (
  `billId` bigint(20) NOT NULL,
  `billType` int(11) NOT NULL,
  `changeAfter` bigint(20) NOT NULL,
  `changeBefore` bigint(20) NOT NULL,
  `changeCount` int(11) NOT NULL,
  `orderId` bigint(20) NOT NULL,
  `orderType` int(11) NOT NULL,
  `uid` bigint(20) NOT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`billId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- game.`user` definition

-- game.`user` definition

CREATE TABLE `user` (
  `uid` bigint(20) NOT NULL,
  `name` varchar(45) NOT NULL DEFAULT '#',
  `nickName` varchar(45) NOT NULL DEFAULT '#',
  `sex` int(11) NOT NULL DEFAULT '1',
  `psw` varchar(45) NOT NULL DEFAULT '#',
  `bankPsw` varchar(45) NOT NULL DEFAULT '#',
  `faceType` int(11) NOT NULL DEFAULT '-1',
  `level` int(11) NOT NULL DEFAULT '0',
  `qq` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '#',
  `wechat` varchar(45) NOT NULL DEFAULT '#',
  `ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '#',
  `city` varchar(255) NOT NULL DEFAULT '#',
  `moblie` varchar(32) NOT NULL DEFAULT '#',
  `headPic` varchar(100) NOT NULL DEFAULT '#',
  `signInfo` varchar(450) NOT NULL DEFAULT '',
  `reMark` varchar(100) NOT NULL DEFAULT '',
  `imei` varchar(100) NOT NULL DEFAULT '#',
  `sdkKey` varchar(100) NOT NULL DEFAULT '#',
  `state` int(11) NOT NULL DEFAULT '1' COMMENT '??????',
  `opUser` varchar(45) NOT NULL DEFAULT 'system',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uid_UNIQUE` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- `game-teenpatti`.room definition
CREATE TABLE `room` (
  `roomId` int(11) NOT NULL COMMENT 'roomid gameType*10 + roomType',
  `gameType` int(11) NOT NULL DEFAULT '0' COMMENT '????????????',
  `roomType` int(11) NOT NULL DEFAULT '0' COMMENT '????????????',
  `reMark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '??????',
  `isValid` int(11) NOT NULL DEFAULT '1',
  `opUser` varchar(45) NOT NULL DEFAULT 'system',
  `ante` bigint(20) NOT NULL COMMENT '??????',
  `siteCount` int(11) NOT NULL COMMENT '?????????',
  `maxDeskCount` int(11) NOT NULL COMMENT '?????????????????????????????????',
  `minPlayersCount` int(11) NOT NULL COMMENT '?????????????????????',
  `minJoin` bigint(20) NOT NULL COMMENT '????????????',
  `minBet` bigint(20) NOT NULL COMMENT '????????????',
  `maxBet` bigint(20) NOT NULL COMMENT '????????????',
   `maxLimit` int(11) DEFAULT '0' COMMENT '??????????????????',
  `taxRate` int(11) NOT NULL COMMENT '???????????????',
  `conf` varchar(200) DEFAULT NULL COMMENT 'json??????',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`roomId`),
  UNIQUE KEY `roomId_UNIQUE` (`roomId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- `game-teenpatti`.room definition

CREATE TABLE `game` (
  `gameType` int(11) NOT NULL COMMENT '????????????',
  `serverType` int(11) NOT NULL COMMENT '?????????????????????',
  `open` int(11) NOT NULL DEFAULT '1',
  `reMark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '??????',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`gameType`),
  UNIQUE KEY `gameType_UNIQUE` (`gameType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
