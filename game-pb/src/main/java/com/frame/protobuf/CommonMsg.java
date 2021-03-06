// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common.proto

package com.frame.protobuf;

public final class CommonMsg {
  private CommonMsg() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  /**
   * Protobuf enum {@code ErrorCode}
   */
  public enum ErrorCode
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <pre>
     *正常
     * </pre>
     *
     * <code>NORMAL = 0;</code>
     */
    NORMAL(0),
    /**
     * <pre>
     *系统错误
     * </pre>
     *
     * <code>SYSTEM_ERR = 400;</code>
     */
    SYSTEM_ERR(400),
    /**
     * <pre>
     *参数错误
     * </pre>
     *
     * <code>ARGS_ERR = 401;</code>
     */
    ARGS_ERR(401),
    /**
     * <pre>
     *token错误
     * </pre>
     *
     * <code>TOKEN_ERR = 402;</code>
     */
    TOKEN_ERR(402),
    /**
     * <pre>
     *你的账号在其他地方登录
     * </pre>
     *
     * <code>OTHER_LOGIN_ERR = 403;</code>
     */
    OTHER_LOGIN_ERR(403),
    /**
     * <pre>
     *超出限制
     * </pre>
     *
     * <code>EXCEEDS_LIMIT = 404;</code>
     */
    EXCEEDS_LIMIT(404),
    /**
     * <pre>
     *余额不足
     * </pre>
     *
     * <code>BALANCE_NOT_ENOUGH = 405;</code>
     */
    BALANCE_NOT_ENOUGH(405),
    UNRECOGNIZED(-1),
    ;

    /**
     * <pre>
     *正常
     * </pre>
     *
     * <code>NORMAL = 0;</code>
     */
    public static final int NORMAL_VALUE = 0;
    /**
     * <pre>
     *系统错误
     * </pre>
     *
     * <code>SYSTEM_ERR = 400;</code>
     */
    public static final int SYSTEM_ERR_VALUE = 400;
    /**
     * <pre>
     *参数错误
     * </pre>
     *
     * <code>ARGS_ERR = 401;</code>
     */
    public static final int ARGS_ERR_VALUE = 401;
    /**
     * <pre>
     *token错误
     * </pre>
     *
     * <code>TOKEN_ERR = 402;</code>
     */
    public static final int TOKEN_ERR_VALUE = 402;
    /**
     * <pre>
     *你的账号在其他地方登录
     * </pre>
     *
     * <code>OTHER_LOGIN_ERR = 403;</code>
     */
    public static final int OTHER_LOGIN_ERR_VALUE = 403;
    /**
     * <pre>
     *超出限制
     * </pre>
     *
     * <code>EXCEEDS_LIMIT = 404;</code>
     */
    public static final int EXCEEDS_LIMIT_VALUE = 404;
    /**
     * <pre>
     *余额不足
     * </pre>
     *
     * <code>BALANCE_NOT_ENOUGH = 405;</code>
     */
    public static final int BALANCE_NOT_ENOUGH_VALUE = 405;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static ErrorCode valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static ErrorCode forNumber(int value) {
      switch (value) {
        case 0: return NORMAL;
        case 400: return SYSTEM_ERR;
        case 401: return ARGS_ERR;
        case 402: return TOKEN_ERR;
        case 403: return OTHER_LOGIN_ERR;
        case 404: return EXCEEDS_LIMIT;
        case 405: return BALANCE_NOT_ENOUGH;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<ErrorCode>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        ErrorCode> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<ErrorCode>() {
            public ErrorCode findValueByNumber(int number) {
              return ErrorCode.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalStateException(
            "Can't get the descriptor of an unrecognized enum value.");
      }
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.frame.protobuf.CommonMsg.getDescriptor().getEnumTypes().get(0);
    }

    private static final ErrorCode[] VALUES = values();

    public static ErrorCode valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private ErrorCode(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:ErrorCode)
  }

  /**
   * <pre>
   *卡牌花色
   * </pre>
   *
   * Protobuf enum {@code CardType}
   */
  public enum CardType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <pre>
     *无花色
     * </pre>
     *
     * <code>NONE = 0;</code>
     */
    NONE(0),
    /**
     * <pre>
     * ( "方块",  1 ),
     * </pre>
     *
     * <code>DIAMOND = 1;</code>
     */
    DIAMOND(1),
    /**
     * <pre>
     * ( "梅花",  2 ),
     * </pre>
     *
     * <code>CLUB = 2;</code>
     */
    CLUB(2),
    /**
     * <pre>
     * ( "黑桃",  3 ),
     * </pre>
     *
     * <code>SPADE = 3;</code>
     */
    SPADE(3),
    /**
     * <pre>
     * ( "红桃",  4 ),
     * </pre>
     *
     * <code>HEART = 4;</code>
     */
    HEART(4),
    /**
     * <pre>
     *( "筒",  5 ),
     * </pre>
     *
     * <code>TONG = 5;</code>
     */
    TONG(5),
    /**
     * <pre>
     * ( "条",  6 ),
     * </pre>
     *
     * <code>TIAO = 6;</code>
     */
    TIAO(6),
    /**
     * <pre>
     *( "万",  7 ),
     * </pre>
     *
     * <code>WAN = 7;</code>
     */
    WAN(7),
    UNRECOGNIZED(-1),
    ;

    /**
     * <pre>
     *无花色
     * </pre>
     *
     * <code>NONE = 0;</code>
     */
    public static final int NONE_VALUE = 0;
    /**
     * <pre>
     * ( "方块",  1 ),
     * </pre>
     *
     * <code>DIAMOND = 1;</code>
     */
    public static final int DIAMOND_VALUE = 1;
    /**
     * <pre>
     * ( "梅花",  2 ),
     * </pre>
     *
     * <code>CLUB = 2;</code>
     */
    public static final int CLUB_VALUE = 2;
    /**
     * <pre>
     * ( "黑桃",  3 ),
     * </pre>
     *
     * <code>SPADE = 3;</code>
     */
    public static final int SPADE_VALUE = 3;
    /**
     * <pre>
     * ( "红桃",  4 ),
     * </pre>
     *
     * <code>HEART = 4;</code>
     */
    public static final int HEART_VALUE = 4;
    /**
     * <pre>
     *( "筒",  5 ),
     * </pre>
     *
     * <code>TONG = 5;</code>
     */
    public static final int TONG_VALUE = 5;
    /**
     * <pre>
     * ( "条",  6 ),
     * </pre>
     *
     * <code>TIAO = 6;</code>
     */
    public static final int TIAO_VALUE = 6;
    /**
     * <pre>
     *( "万",  7 ),
     * </pre>
     *
     * <code>WAN = 7;</code>
     */
    public static final int WAN_VALUE = 7;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static CardType valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static CardType forNumber(int value) {
      switch (value) {
        case 0: return NONE;
        case 1: return DIAMOND;
        case 2: return CLUB;
        case 3: return SPADE;
        case 4: return HEART;
        case 5: return TONG;
        case 6: return TIAO;
        case 7: return WAN;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<CardType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        CardType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<CardType>() {
            public CardType findValueByNumber(int number) {
              return CardType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalStateException(
            "Can't get the descriptor of an unrecognized enum value.");
      }
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.frame.protobuf.CommonMsg.getDescriptor().getEnumTypes().get(1);
    }

    private static final CardType[] VALUES = values();

    public static CardType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private CardType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:CardType)
  }

  /**
   * <pre>
   *卡牌数值
   * </pre>
   *
   * Protobuf enum {@code CardValue}
   */
  public enum CardValue
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <pre>
     *无意义
     * </pre>
     *
     * <code>VAULE_N = 0;</code>
     */
    VAULE_N(0),
    /**
     * <pre>
     *  ( "A", 1, 14, 14 ),
     * </pre>
     *
     * <code>VAULE_A = 1;</code>
     */
    VAULE_A(1),
    /**
     * <pre>
     * ( "2", 2, 15, 2 ),
     * </pre>
     *
     * <code>VAULE_2 = 2;</code>
     */
    VAULE_2(2),
    /**
     * <pre>
     * ( "3", 3, 3, 3 ),
     * </pre>
     *
     * <code>VAULE_3 = 3;</code>
     */
    VAULE_3(3),
    /**
     * <pre>
     * ( "4", 4, 4, 4 ),
     * </pre>
     *
     * <code>VAULE_4 = 4;</code>
     */
    VAULE_4(4),
    /**
     * <pre>
     * ( "5", 5, 5, 5 ),
     * </pre>
     *
     * <code>VAULE_5 = 5;</code>
     */
    VAULE_5(5),
    /**
     * <pre>
     * ( "6", 6, 6, 6 ),
     * </pre>
     *
     * <code>VAULE_6 = 6;</code>
     */
    VAULE_6(6),
    /**
     * <pre>
     * ( "7", 7, 7, 7 ),
     * </pre>
     *
     * <code>VAULE_7 = 7;</code>
     */
    VAULE_7(7),
    /**
     * <pre>
     * ( "8", 8, 8, 8 ),
     * </pre>
     *
     * <code>VAULE_8 = 8;</code>
     */
    VAULE_8(8),
    /**
     * <pre>
     * ( "9", 9, 9, 9 ),
     * </pre>
     *
     * <code>VAULE_9 = 9;</code>
     */
    VAULE_9(9),
    /**
     * <pre>
     * ( "10", 10, 10, 10 ),
     * </pre>
     *
     * <code>VAULE_10 = 10;</code>
     */
    VAULE_10(10),
    /**
     * <pre>
     * ( "J", 11, 11, 11 ),
     * </pre>
     *
     * <code>VAULE_J = 11;</code>
     */
    VAULE_J(11),
    /**
     * <pre>
     * ( "Q", 12, 12, 12 ),
     * </pre>
     *
     * <code>VAULE_Q = 12;</code>
     */
    VAULE_Q(12),
    /**
     * <pre>
     *  ( "K", 13, 13, 13 ),
     * </pre>
     *
     * <code>VAULE_K = 13;</code>
     */
    VAULE_K(13),
    /**
     * <pre>
     *  "小鬼", 16, 16, 16 ),
     * </pre>
     *
     * <code>VAULE_SK = 14;</code>
     */
    VAULE_SK(14),
    /**
     * <pre>
     *  ( "大鬼", 17, 17, 17 ),
     * </pre>
     *
     * <code>VAULE_BK = 15;</code>
     */
    VAULE_BK(15),
    UNRECOGNIZED(-1),
    ;

    /**
     * <pre>
     *无意义
     * </pre>
     *
     * <code>VAULE_N = 0;</code>
     */
    public static final int VAULE_N_VALUE = 0;
    /**
     * <pre>
     *  ( "A", 1, 14, 14 ),
     * </pre>
     *
     * <code>VAULE_A = 1;</code>
     */
    public static final int VAULE_A_VALUE = 1;
    /**
     * <pre>
     * ( "2", 2, 15, 2 ),
     * </pre>
     *
     * <code>VAULE_2 = 2;</code>
     */
    public static final int VAULE_2_VALUE = 2;
    /**
     * <pre>
     * ( "3", 3, 3, 3 ),
     * </pre>
     *
     * <code>VAULE_3 = 3;</code>
     */
    public static final int VAULE_3_VALUE = 3;
    /**
     * <pre>
     * ( "4", 4, 4, 4 ),
     * </pre>
     *
     * <code>VAULE_4 = 4;</code>
     */
    public static final int VAULE_4_VALUE = 4;
    /**
     * <pre>
     * ( "5", 5, 5, 5 ),
     * </pre>
     *
     * <code>VAULE_5 = 5;</code>
     */
    public static final int VAULE_5_VALUE = 5;
    /**
     * <pre>
     * ( "6", 6, 6, 6 ),
     * </pre>
     *
     * <code>VAULE_6 = 6;</code>
     */
    public static final int VAULE_6_VALUE = 6;
    /**
     * <pre>
     * ( "7", 7, 7, 7 ),
     * </pre>
     *
     * <code>VAULE_7 = 7;</code>
     */
    public static final int VAULE_7_VALUE = 7;
    /**
     * <pre>
     * ( "8", 8, 8, 8 ),
     * </pre>
     *
     * <code>VAULE_8 = 8;</code>
     */
    public static final int VAULE_8_VALUE = 8;
    /**
     * <pre>
     * ( "9", 9, 9, 9 ),
     * </pre>
     *
     * <code>VAULE_9 = 9;</code>
     */
    public static final int VAULE_9_VALUE = 9;
    /**
     * <pre>
     * ( "10", 10, 10, 10 ),
     * </pre>
     *
     * <code>VAULE_10 = 10;</code>
     */
    public static final int VAULE_10_VALUE = 10;
    /**
     * <pre>
     * ( "J", 11, 11, 11 ),
     * </pre>
     *
     * <code>VAULE_J = 11;</code>
     */
    public static final int VAULE_J_VALUE = 11;
    /**
     * <pre>
     * ( "Q", 12, 12, 12 ),
     * </pre>
     *
     * <code>VAULE_Q = 12;</code>
     */
    public static final int VAULE_Q_VALUE = 12;
    /**
     * <pre>
     *  ( "K", 13, 13, 13 ),
     * </pre>
     *
     * <code>VAULE_K = 13;</code>
     */
    public static final int VAULE_K_VALUE = 13;
    /**
     * <pre>
     *  "小鬼", 16, 16, 16 ),
     * </pre>
     *
     * <code>VAULE_SK = 14;</code>
     */
    public static final int VAULE_SK_VALUE = 14;
    /**
     * <pre>
     *  ( "大鬼", 17, 17, 17 ),
     * </pre>
     *
     * <code>VAULE_BK = 15;</code>
     */
    public static final int VAULE_BK_VALUE = 15;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static CardValue valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static CardValue forNumber(int value) {
      switch (value) {
        case 0: return VAULE_N;
        case 1: return VAULE_A;
        case 2: return VAULE_2;
        case 3: return VAULE_3;
        case 4: return VAULE_4;
        case 5: return VAULE_5;
        case 6: return VAULE_6;
        case 7: return VAULE_7;
        case 8: return VAULE_8;
        case 9: return VAULE_9;
        case 10: return VAULE_10;
        case 11: return VAULE_J;
        case 12: return VAULE_Q;
        case 13: return VAULE_K;
        case 14: return VAULE_SK;
        case 15: return VAULE_BK;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<CardValue>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        CardValue> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<CardValue>() {
            public CardValue findValueByNumber(int number) {
              return CardValue.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalStateException(
            "Can't get the descriptor of an unrecognized enum value.");
      }
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.frame.protobuf.CommonMsg.getDescriptor().getEnumTypes().get(2);
    }

    private static final CardValue[] VALUES = values();

    public static CardValue valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private CardValue(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:CardValue)
  }

  /**
   * <pre>
   *服务器类型
   * </pre>
   *
   * Protobuf enum {@code ServerType}
   */
  public enum ServerType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <pre>
     *登录服务器
     * </pre>
     *
     * <code>LOGIN = 0;</code>
     */
    LOGIN(0),
    /**
     * <pre>
     *网关服务器
     * </pre>
     *
     * <code>GATEWAY = 1;</code>
     */
    GATEWAY(1),
    /**
     * <pre>
     *大厅服务器
     * </pre>
     *
     * <code>CENTER = 2;</code>
     */
    CENTER(2),
    /**
     * <pre>
     *teenpatti 游戏服务器
     * </pre>
     *
     * <code>TEENPATTI = 3;</code>
     */
    TEENPATTI(3),
    /**
     * <pre>
     *teenpatti 游戏服务器
     * </pre>
     *
     * <code>PVE = 10;</code>
     */
    PVE(10),
    UNRECOGNIZED(-1),
    ;

    /**
     * <pre>
     *登录服务器
     * </pre>
     *
     * <code>LOGIN = 0;</code>
     */
    public static final int LOGIN_VALUE = 0;
    /**
     * <pre>
     *网关服务器
     * </pre>
     *
     * <code>GATEWAY = 1;</code>
     */
    public static final int GATEWAY_VALUE = 1;
    /**
     * <pre>
     *大厅服务器
     * </pre>
     *
     * <code>CENTER = 2;</code>
     */
    public static final int CENTER_VALUE = 2;
    /**
     * <pre>
     *teenpatti 游戏服务器
     * </pre>
     *
     * <code>TEENPATTI = 3;</code>
     */
    public static final int TEENPATTI_VALUE = 3;
    /**
     * <pre>
     *teenpatti 游戏服务器
     * </pre>
     *
     * <code>PVE = 10;</code>
     */
    public static final int PVE_VALUE = 10;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static ServerType valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static ServerType forNumber(int value) {
      switch (value) {
        case 0: return LOGIN;
        case 1: return GATEWAY;
        case 2: return CENTER;
        case 3: return TEENPATTI;
        case 10: return PVE;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<ServerType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        ServerType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<ServerType>() {
            public ServerType findValueByNumber(int number) {
              return ServerType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalStateException(
            "Can't get the descriptor of an unrecognized enum value.");
      }
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.frame.protobuf.CommonMsg.getDescriptor().getEnumTypes().get(3);
    }

    private static final ServerType[] VALUES = values();

    public static ServerType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private ServerType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:ServerType)
  }

  /**
   * <pre>
   *游戏类型
   * </pre>
   *
   * Protobuf enum {@code GameType}
   */
  public enum GameType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <pre>
     *占位符无意义
     * </pre>
     *
     * <code>GAME_NONE = 0;</code>
     */
    GAME_NONE(0),
    /**
     * <pre>
     *TEENPATTI
     * </pre>
     *
     * <code>GAME_TEENPATTI = 1;</code>
     */
    GAME_TEENPATTI(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <pre>
     *占位符无意义
     * </pre>
     *
     * <code>GAME_NONE = 0;</code>
     */
    public static final int GAME_NONE_VALUE = 0;
    /**
     * <pre>
     *TEENPATTI
     * </pre>
     *
     * <code>GAME_TEENPATTI = 1;</code>
     */
    public static final int GAME_TEENPATTI_VALUE = 1;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static GameType valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static GameType forNumber(int value) {
      switch (value) {
        case 0: return GAME_NONE;
        case 1: return GAME_TEENPATTI;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<GameType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        GameType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<GameType>() {
            public GameType findValueByNumber(int number) {
              return GameType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalStateException(
            "Can't get the descriptor of an unrecognized enum value.");
      }
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.frame.protobuf.CommonMsg.getDescriptor().getEnumTypes().get(4);
    }

    private static final GameType[] VALUES = values();

    public static GameType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private GameType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:GameType)
  }

  /**
   * Protobuf enum {@code RoomType}
   */
  public enum RoomType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <pre>
     *低级场
     * </pre>
     *
     * <code>GAME_LOW = 0;</code>
     */
    GAME_LOW(0),
    /**
     * <pre>
     *中级场
     * </pre>
     *
     * <code>GAME_MID = 1;</code>
     */
    GAME_MID(1),
    /**
     * <pre>
     *高级场
     * </pre>
     *
     * <code>GAME_HIGHT = 2;</code>
     */
    GAME_HIGHT(2),
    /**
     * <pre>
     *私人场
     * </pre>
     *
     * <code>GAME_PERSON = 5;</code>
     */
    GAME_PERSON(5),
    UNRECOGNIZED(-1),
    ;

    /**
     * <pre>
     *低级场
     * </pre>
     *
     * <code>GAME_LOW = 0;</code>
     */
    public static final int GAME_LOW_VALUE = 0;
    /**
     * <pre>
     *中级场
     * </pre>
     *
     * <code>GAME_MID = 1;</code>
     */
    public static final int GAME_MID_VALUE = 1;
    /**
     * <pre>
     *高级场
     * </pre>
     *
     * <code>GAME_HIGHT = 2;</code>
     */
    public static final int GAME_HIGHT_VALUE = 2;
    /**
     * <pre>
     *私人场
     * </pre>
     *
     * <code>GAME_PERSON = 5;</code>
     */
    public static final int GAME_PERSON_VALUE = 5;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static RoomType valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static RoomType forNumber(int value) {
      switch (value) {
        case 0: return GAME_LOW;
        case 1: return GAME_MID;
        case 2: return GAME_HIGHT;
        case 5: return GAME_PERSON;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<RoomType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        RoomType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<RoomType>() {
            public RoomType findValueByNumber(int number) {
              return RoomType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalStateException(
            "Can't get the descriptor of an unrecognized enum value.");
      }
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.frame.protobuf.CommonMsg.getDescriptor().getEnumTypes().get(5);
    }

    private static final RoomType[] VALUES = values();

    public static RoomType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private RoomType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:RoomType)
  }


  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014common.proto*\212\001\n\tErrorCode\022\n\n\006NORMAL\020\000" +
      "\022\017\n\nSYSTEM_ERR\020\220\003\022\r\n\010ARGS_ERR\020\221\003\022\016\n\tTOKE" +
      "N_ERR\020\222\003\022\024\n\017OTHER_LOGIN_ERR\020\223\003\022\022\n\rEXCEED" +
      "S_LIMIT\020\224\003\022\027\n\022BALANCE_NOT_ENOUGH\020\225\003*^\n\010C" +
      "ardType\022\010\n\004NONE\020\000\022\013\n\007DIAMOND\020\001\022\010\n\004CLUB\020\002" +
      "\022\t\n\005SPADE\020\003\022\t\n\005HEART\020\004\022\010\n\004TONG\020\005\022\010\n\004TIAO" +
      "\020\006\022\007\n\003WAN\020\007*\336\001\n\tCardValue\022\013\n\007VAULE_N\020\000\022\013" +
      "\n\007VAULE_A\020\001\022\013\n\007VAULE_2\020\002\022\013\n\007VAULE_3\020\003\022\013\n" +
      "\007VAULE_4\020\004\022\013\n\007VAULE_5\020\005\022\013\n\007VAULE_6\020\006\022\013\n\007" +
      "VAULE_7\020\007\022\013\n\007VAULE_8\020\010\022\013\n\007VAULE_9\020\t\022\014\n\010V" +
      "AULE_10\020\n\022\013\n\007VAULE_J\020\013\022\013\n\007VAULE_Q\020\014\022\013\n\007V" +
      "AULE_K\020\r\022\014\n\010VAULE_SK\020\016\022\014\n\010VAULE_BK\020\017*H\n\n" +
      "ServerType\022\t\n\005LOGIN\020\000\022\013\n\007GATEWAY\020\001\022\n\n\006CE" +
      "NTER\020\002\022\r\n\tTEENPATTI\020\003\022\007\n\003PVE\020\n*-\n\010GameTy" +
      "pe\022\r\n\tGAME_NONE\020\000\022\022\n\016GAME_TEENPATTI\020\001*G\n" +
      "\010RoomType\022\014\n\010GAME_LOW\020\000\022\014\n\010GAME_MID\020\001\022\016\n" +
      "\nGAME_HIGHT\020\002\022\017\n\013GAME_PERSON\020\005B\037\n\022com.fr" +
      "ame.protobufB\tCommonMsgb\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
