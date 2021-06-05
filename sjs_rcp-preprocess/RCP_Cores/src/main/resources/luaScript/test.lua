-- 获取Key
local setKey = KEYS[1]

local tag = string.sub(KEYS[1], string.find(KEYS[1],"{",1), string.find(KEYS[1],"}",1))
-- 申报购买：order | 申报撤单：cancel | 成交信息：trade
local dataType =  string.sub(ARGV[1], 2, string.find(ARGV[1],"{",1)-1)
-- 证券6位国内代码
local instrumentId = string.sub(ARGV[2], 2, string.find(ARGV[2],"{",1)-1) + 0
-- 订单号码
local ordrNum = string.sub(ARGV[3], 2, string.find(ARGV[3],"{",1)-1)
-- 投资者账户
local invAcctId = string.sub(ARGV[4], 2, string.find(ARGV[4],"{",1)-1)
-- 订单价格（限价）
local ordrExePrc = string.sub(ARGV[5], 2, string.find(ARGV[5],"{",1)-1) + 0
-- 剩余订单有效数量
local ordrQty = string.sub(ARGV[6], 2, string.find(ARGV[6],"{",1)-1) + 0
-- 该产品当前价的有效剩余申报量
local ordrQtyTotal = string.sub(ARGV[7], 2, string.find(ARGV[7],"{",1)-1) + 0
-- 该产品的当前价的累计执行数量
local ordrExeQtyTotal = string.sub(ARGV[8], 2, string.find(ARGV[8],"{",1)-1) + 0

-- 常量
local tag = string.sub(KEYS[1], string.find(KEYS[1],"{",1), string.find(KEYS[1],"}",1))              -- hash tag
local orderType = "order"       -- OC申报
local cancelType = "cancel"     -- OC撤单
local tradeType = "trade"       -- TC成交
-- hash key 常量
local instrumentIdStr = "instrumentId"      -- 证券6位国内代码
local ordrNumStr = "ordrNum"                -- 订单号码
local invAcctIdStr = "invAcctId"            -- 投资者账户
local ordrExePrcStr = "ordrExePrc"          -- 订单价格
local ordrQtyStr = "ordrQty"                -- 剩余订单有效数量
local ordrQtyTotalStr = "ordrQtyTotal"      -- 该产品当前价的有效剩余申报量
local ordrExeQtyTotalStr = "ordrExeQtyTotal"    -- 该产品的当前价的累计执行数量
local orderCountsStr = "orderCounts"    -- 剩余订单笔数

local tt ={}
tt[1]  =cjson.encode(setKey)
tt[2]  =cjson.encode(KEYS[1])
tt[3]  =cjson.encode(dataType)
tt[4]  =cjson.encode(ARGV[1])
tt[5]  =cjson.encode(instrumentId)
tt[6]  =cjson.encode(ARGV[2])
tt[7]  =cjson.encode(ordrNum)
tt[8]  =cjson.encode(ARGV[3])
tt[9]  =cjson.encode(invAcctId)
tt[10] =cjson.encode(ARGV[4])
tt[11] =cjson.encode(ordrExePrc)
tt[12] =cjson.encode(ARGV[5])
tt[13] =cjson.encode(ordrQty)
tt[14] =cjson.encode(ARGV[6])
tt[15] =cjson.encode(ordrQtyTotal)
tt[16] =cjson.encode(ARGV[7])
tt[17] =cjson.encode(ordrExeQtyTotal)
tt[18] =cjson.encode(ARGV[8])
tt[19] =cjson.encode(tag)
-- return  cjson.encode(tt)
return tt
