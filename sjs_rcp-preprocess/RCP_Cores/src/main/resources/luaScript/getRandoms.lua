-- 获取Key
local setKey = KEYS[1]
-- redis.log(redis.LOG_NOTICE,KEYS[1])

-- 获取参数
local currentSeq = string.sub(ARGV[1], 2, string.find(ARGV[1],"{",1)-1) + 0
local limit = string.sub(ARGV[2], 2, string.find(ARGV[2],"{",1)-1) + 0
local discardKey = string.sub(ARGV[3], 2, string.len(ARGV[3])-1)

-- 从启始seq开始查询redis中是否有数据，循环查出一批符合顺序的数据
local dataList = {}
local idx = 1
local disCount
repeat
    local dataTemp = redis.call("HGET",setKey,currentSeq)
    if(dataTemp ~= false)
    then  -- 如果当前seq有数据，则正常处理
        dataList[idx] = dataTemp
        idx = idx + 1
        -- 删除掉已经读取过的数据，防止内存泄漏
        redis.call("HDEL",setKey,currentSeq)
    else
        disCount = redis.call("SISMEMBER",discardKey,currentSeq)
--        if(disCount > 0)
--        then
--            -- 删除掉已经读取过的数据，防止内存泄漏
--            redis.call("SREM",setKey,currentSeq)
--        end
    end
    currentSeq = currentSeq + 1
until(idx > limit or (dataTemp == false and disCount ~= nil and disCount < 1))
-- 如果达到限制条数 或 取不到连续的数据，则结束本次取值（如果取不到值的seq是discard序号，视为连续）

-- 要返回的变量
return dataList