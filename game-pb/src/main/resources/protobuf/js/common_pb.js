// source: common.proto
/**
 * @fileoverview
 * @enhanceable
 * @suppress {missingRequire} reports error on implicit type usages.
 * @suppress {messageConventions} JS Compiler reports an error if a variable or
 *     field starts with 'MSG_' and isn't a translatable message.
 * @public
 */
// GENERATED CODE -- DO NOT EDIT!
/* eslint-disable */
// @ts-nocheck

var jspb = require('google-protobuf');
var goog = jspb;
var global = (function() {
  if (this) { return this; }
  if (typeof window !== 'undefined') { return window; }
  if (typeof global !== 'undefined') { return global; }
  if (typeof self !== 'undefined') { return self; }
  return Function('return this')();
}.call(null));

goog.exportSymbol('proto.CardType', null, global);
goog.exportSymbol('proto.CardValue', null, global);
goog.exportSymbol('proto.ErrorCode', null, global);
goog.exportSymbol('proto.GameType', null, global);
goog.exportSymbol('proto.RoomType', null, global);
goog.exportSymbol('proto.ServerType', null, global);
/**
 * @enum {number}
 */
proto.ErrorCode = {
  NORMAL: 0,
  SYSTEM_ERR: 400,
  ARGS_ERR: 401,
  TOKEN_ERR: 402,
  OTHER_LOGIN_ERR: 403,
  EXCEEDS_LIMIT: 404,
  BALANCE_NOT_ENOUGH: 405
};

/**
 * @enum {number}
 */
proto.CardType = {
  NONE: 0,
  DIAMOND: 1,
  CLUB: 2,
  SPADE: 3,
  HEART: 4,
  TONG: 5,
  TIAO: 6,
  WAN: 7
};

/**
 * @enum {number}
 */
proto.CardValue = {
  VAULE_N: 0,
  VAULE_A: 1,
  VAULE_2: 2,
  VAULE_3: 3,
  VAULE_4: 4,
  VAULE_5: 5,
  VAULE_6: 6,
  VAULE_7: 7,
  VAULE_8: 8,
  VAULE_9: 9,
  VAULE_10: 10,
  VAULE_J: 11,
  VAULE_Q: 12,
  VAULE_K: 13,
  VAULE_SK: 14,
  VAULE_BK: 15
};

/**
 * @enum {number}
 */
proto.ServerType = {
  LOGIN: 0,
  GATEWAY: 1,
  CENTER: 2,
  TEENPATTI: 3,
  PVE: 10
};

/**
 * @enum {number}
 */
proto.GameType = {
  GAME_NONE: 0,
  GAME_TEENPATTI: 1
};

/**
 * @enum {number}
 */
proto.RoomType = {
  GAME_LOW: 0,
  GAME_MID: 1,
  GAME_HIGHT: 2,
  GAME_PERSON: 5
};

goog.object.extend(exports, proto);
