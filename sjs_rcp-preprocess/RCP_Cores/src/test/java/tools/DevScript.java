package tools;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * 开发脚本，不能release
 */
public class DevScript {

    /**
     * 批量生成setter方法
     *
     * @param fromClazz     数据来源的class
     * @param fromObjName   数据来源对象名称
     * @param targetClazz   要写入对象的class
     * @param targetObjName set对象的名称
     */
    public static void setterScrpt(Class fromClazz, String fromObjName, Class targetClazz, String targetObjName) {
        StringBuffer sb = new StringBuffer();
        String prefixTarget = targetObjName + ".set", prefixFrom = fromObjName + ".get";

        for (Field f : fromClazz.getDeclaredFields()) {
            String methodName = f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            sb.append(prefixTarget).append(methodName).append("(")
                    .append(prefixFrom).append(methodName).append("()").append(");").append("\r\n");
        }
        // 生成的setter打印在控制台
        System.out.println(sb.toString());
    }

    // setterScrpt的测试方法
//    public static void main(String[] args) {
//        setterScrpt(SimpTcT.class, "simpF", SimpTcT.class, "simpT");
//    }

    public static void main(String[] args) {
        List<Integer> list = new LinkedList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        for (int i : list){
            System.out.println(i);
        }
        System.out.println("------");
//        list.set(0,0);

        list.add(0,0);
        for (int i : list){
            System.out.println(i);
        }
    }


//    /**
//     * 将objFrom的属性复制给objTo
//     *
//     * @param objFrom 属性值来源
//     * @param objTo   赋值目标
//     * @throws NoSuchMethodException
//     * @throws InvocationTargetException
//     * @throws IllegalAccessException
//     */
//    public static void filedCopy(Object objFrom, Object objTo) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        Assert.noNullElements(new Object[]{objFrom, objTo}, "from和to对象都不能为空！");
//
//        Field[] fs = objFrom.getClass().getDeclaredFields();
//        for (Field f : fs) {
//            String nameFrom = f.getName();
//            Class<?> clazzFrom = objFrom.getClass();
//            String methodNameFrom = "get" + nameFrom.substring(0, 1).toUpperCase() + nameFrom.substring(1);
//            Method methodFrom = clazzFrom.getDeclaredMethod(methodNameFrom, null);
//            Object fVal = methodFrom.invoke(objFrom, null);
//
//            Class<?> clazzTo = objTo.getClass();
//            String methodNameTo = "s" + methodNameFrom.substring(1);
//            Method methodTo = clazzTo.getDeclaredMethod(methodNameTo, f.getType());
//            methodTo.invoke(objTo, fVal);
//        }
//    }
//
//
//    /**
//     * 方法测试入口
//     * @param args
//     * @throws NoSuchMethodException
//     * @throws IllegalAccessException
//     * @throws InvocationTargetException
//     */
//    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        Test from = new Test();
//        from.setIin(3);
//        from.setCc('c');
//        from.setInteger(56);
//        from.setLon(22L);
//        from.setStr("str");
//        from.setObject(String.valueOf("测试"));
//
//        Test to = new Test();
//        filedCopy(from,to);
//        System.out.println(to.toString());
//    }
//
//    /**
//     * 测试类
//     */
//    @Data
//    public static class Test {
//        private String str;
//        private Object object;
//        private Integer integer;
//        private long lon;
//        private char cc;
//        private int iin;
//    }
}
