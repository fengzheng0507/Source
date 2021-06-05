-- 测试命令:
-- redis-cli -a sf@sf12dds -c -h 10.112.103.5 -p 7001 --ldb --eval tb.lua key{t} , "\"order{t}\"" "\"123456{t}\"" "\"906{t}\"" "\"666{t}\"" "\"100.12345{t}\"" "\"101.123{t}\"" "\"102.123{t}\"" "\"103.123{t}\""

-- 获取Key
local setKey = KEYS[1]

-- 获取参数
-- hash tag
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
local ordrExePrc = string.sub(ARGV[5], 2, string.find(ARGV[5],"{",1)-1) + 0.00000
-- 剩余订单有效数量
local ordrQty = string.sub(ARGV[6], 2, string.find(ARGV[6],"{",1)-1) + 0.000
-- 该产品当前价的有效剩余申报量
local ordrQtyTotal = string.sub(ARGV[7], 2, string.find(ARGV[7],"{",1)-1) + 0.000
-- 该产品的当前价的累计执行数量
local ordrExeQtyTotal = string.sub(ARGV[8], 2, string.find(ARGV[8],"{",1)-1) + 0.000


-- 常量
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

-- 产品+tag -> 价格链表key
local function function_makePriceChainKey(instrumentId)
    return instrumentId..tag
end

-- 产品+价格+tag -> 价格节点key
local function function_makePriceNodeKey(instrumentId, ordrExePrc)
    return instrumentId.."-"..ordrExePrc..tag
end

-- 订单号码+tag -> 订单节点key
local function function_makeOrderNodeKey(ordrNum)
    return ordrNum..tag
end

-- 价格链表新增节点
local function function_extendPriceChain(instrumentId,ordrExePrc)
    local chainKey = function_makePriceChainKey(instrumentId)
    local chainSize = redis.call("LLEN",chainKey)
    local targetIdx = 0
    local targetPrice = nil

    if(chainSize > 0)
    then    -- 将当前节点插入链表
        if(instrumentId % 2 ~= 0)
        then -- 奇数结尾，则为卖
            while(targetIdx < chainSize and targetPrice == nil)
            do
                local currentPrice = redis.call("LINDEX",chainKey,targetIdx)
                if(ordrExePrc < currentPrice) --此处不会有等号的情况
                then
                    targetPrice = currentPrice
                else
                    targetIdx = targetIdx + 1
                end
            end
        else -- 偶数结尾，则为买
            while(targetIdx < chainSize and targetPrice == nil)
            do
                local currentPrice = redis.call("LINDEX",chainKey,targetIdx)
                if(ordrExePrc > currentPrice) --此处不会有等号的情况
                then
                    targetPrice = currentPrice
                else
                    targetIdx = targetIdx + 1
                end
            end
        end

        if(targetPrice ~= nil)
        then    -- 找到的插入的位置，则插入
            redis.call("LINSERT",chainKey,"BEFORE",targetPrice,ordrExePrc)
        else    -- 说明在已有链表中并没有插入的位置，则追加到链表末尾
            redis.call("RPUSH",chainKey,targetPrice)
        end

    else    -- 新增当前产品的价格链表
        redis.call("RPUSH",chainKey,targetPrice)
    end
end

-- 处理OC申报的函数
local function function_order(instrumentId, ordrNum, invAcctId, ordrExePrc, ordrQty, ordrExeQtyTotal)
    -- 获取当前价格节点数据[证券6位国内代码,订单价格,当前价格节点的订单总笔数,该产品当前价的有效剩余申报量,该产品的当前价的累计执行数量]
    -- local currentDatas = redis.call("HMGET",function_makePriceNodeKey(instrumentId, ordrExePrc),instrumentIdStr,ordrExePrcStr,orderCountsStr,ordrQtyTotalStr,ordrExeQtyTotalStr);
    -- 获取redis key （打上tag
    local priceKey = function_makePriceNodeKey(instrumentId,ordrExePrc)
    -- 获取当前价格节点数据:[当前价格节点的订单总笔数,该产品当前价的有效剩余申报量,该产品的当前价的累计执行数量]
    local currentDatas = redis.call("HMGET",priceKey,orderCountsStr,ordrQtyTotalStr,ordrExeQtyTotalStr)
    if(next(currentDatas) ~= nil and currentDatas[1] ~= false)
    then  -- 如果有价格节点，则处理对应价格节点，然后判断是否调整价格链表（此处为正向OC，则不会删除节点和改变链表结构）
        redis.call("HMSET",priceKey,orderCountsStr,currentDatas[1]+1,ordrQtyTotalStr,currentDatas[2]+ordrQty,ordrExeQtyTotalStr,ordrExeQtyTotal)
    else  -- 如果没有价格节点，则要更新价格链表，并新增价格节点
        redis.call("HMSET",priceKey,orderCountsStr,1,ordrQtyTotalStr,ordrQty,ordrExeQtyTotalStr,ordrExeQtyTotal)
        -- 调整价格链表
        function_extendPriceChain(instrumentId,ordrExePrc)
    end

    -- 新增订单节点
    local orderKey = function_makeOrderNodeKey(ordrNum)
    redis.call("HMSET",orderKey,instrumentIdStr,instrumentId,ordrNumStr,ordrNum,invAcctIdStr,invAcctId,ordrExePrcStr,ordrExePrc,ordrQtyStr,ordrQty)
end

-- 消费订单节点和价格节点
local function function_consumeNode(chainKey, priceKey, orderKey, currentQty, ordrExePrc)
    -- 获取当前价格节点数据:[当前价格节点的订单总笔数,该产品当前价的有效剩余申报量,该产品的当前价的累计执行数量]
    local currentDatas = redis.call("HMGET",priceKey,orderCountsStr,ordrQtyTotalStr,ordrExeQtyTotalStr)
    if(next(currentDatas) ~= nil)
    then    -- 如果有价格节点，则处理对应价格节点，然后判断是否调整价格链表（此处为正向OC，则不会删除节点和改变链表结构）
        if(currentQty > 0)
        then    -- 订单节点还有剩余量
            -- 先更新订单节点
            redis.call("HSET",orderKey,ordrQtyStr,currentQty)
            -- 更新价格节点的量
            redis.call("HMSET",priceKey,ordrQtyTotalStr,currentDatas[2]-ordrQty,ordrExeQtyTotalStr,ordrExeQtyTotal)
        else    -- 一个订单节点需要拿掉
            -- 先删除订单节点
            redis.call("DEL",orderKey)
            -- 价格节点的订单笔数减一
            local currentCount = currentDatas[1] - 1
            if(currentCount > 0)
            then    -- 该价格节点内还有数据，则只更新节点数据
                redis.call("HMSET",priceKey,orderCountsStr,currentCount,ordrQtyTotalStr,currentDatas[2]-ordrQty,ordrExeQtyTotalStr,ordrExeQtyTotal)
            else    -- 该价格节点已经没有订单，则应移除链表中该节点
                redis.call("LREM",chainKey,1,ordrExePrc)
                if(redis.call("LLEN",chainKey) < 1)
                then    -- 如果价格链表已经没有节点，则移除整个链表
                    redis.call("DEL",chainKey)
                end
            end
        end
    else    -- 如果没有撤单信息对应的节点，则数据异常
        error("节点处理异常:["..instrumentId.."]产品在价格["..ordrExePrc.."]上没有["..ordrNum.."]订单号对应的价格节点",2)
    end
end

-- 处理OC撤单的函数
local function function_cancel(instrumentId, ordrNum, invAcctId, ordrExePrc, ordrQty, ordrExeQtyTotal)
    -- 获取redis key （打上tag
    local orderKey = function_makeOrderNodeKey(ordrNum)
    local priceKey = function_makePriceNodeKey(instrumentId,ordrExePrc)
    function_consumeNode(function_makePriceChainKey(instrumentId),priceKey,orderKey,0,ordrExePrc)
end

-- 处理TC成交的函数
local function function_trade(instrumentId, ordrNum, invAcctId, ordrExePrc, ordrQty, ordrExeQtyTotal)
    -- 获取redis key （打上tag
    local priceKey = function_makePriceNodeKey(instrumentId,ordrExePrc)
    local orderKey = function_makeOrderNodeKey(ordrNum)

    -- 先获取当前订单节点的剩余量
    local currentQty = redis.call("HGET",orderKey,ordrQtyStr)
    if(currentQty == false or currentQty == nil)
    then
        error("成交处理异常:["..instrumentId.."]产品没有["..ordrNum.."]订单号对应的订单节点",2)
    else
        currentQty = currentQty - ordrQty
    end

    function_consumeNode(function_makePriceChainKey(instrumentId),priceKey,orderKey,currentQty,ordrExePrc)
end


if(dataType == orderType)
then --如果为OC申报
    function_order(instrumentId, ordrNum, invAcctId, ordrExePrc, ordrQty, ordrExeQtyTotal)
else
    if(dataType == cancelType)
    then --如果为OC撤单
        function_cancel(instrumentId, ordrNum, invAcctId, ordrExePrc, ordrQty, ordrExeQtyTotal)
    else
        if(dataType == tradeType)
        then --如果为TC成交
            function_trade(instrumentId, ordrNum, invAcctId, ordrExePrc, ordrQty, ordrExeQtyTotal)
        else
            -- 如果三种类型都不是，则数据有误
            error("投资者["..invAcctId.."]在["..instrumentId.."]产品的订单编号为["..ordrNum.."]的cnmf数据类型为：["..dataType.."]!",2)
        end
    end
end

-- 最后返回当前产品的买\卖十档行情  -> 提成函数
return "执行结束"