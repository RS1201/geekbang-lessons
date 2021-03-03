package org.geektimes.projects.user.repository;

import org.geektimes.function.ThrowableFunction;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.sql.DBConnectionManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.ClassUtils.wrapperToPrimitive;

public class DatabaseUserRepository implements UserRepository {
    public static final String INSERT_USER_DML_SQL =
            "INSERT INTO users(name,password,email,phoneNumber) VALUES " +
                    "(?,?,?,?)";
    public static final String QUERY_ALL_USERS_DML_SQL =
            "SELECT id,name,password,email,phoneNumber FROM users";
    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> resultSetMethodMappings = new HashMap<>();
    static Map<Class, String> preparedStatementMethodMappings = new HashMap<>();
    private static Logger logger = Logger.getLogger(DatabaseUserRepository.class.getName());
    /**
     * 通用处理方式
     */
    private static Consumer<Throwable> COMMON_EXCEPTION_HANDLER = e -> logger.log(Level.SEVERE, e.getMessage());

    static {
        resultSetMethodMappings.put(Long.class, "getLong");
        resultSetMethodMappings.put(String.class, "getString");

        // long
        preparedStatementMethodMappings.put(Long.class, "setLong");
        preparedStatementMethodMappings.put(String.class, "setString");
    }

    private DBConnectionManager dbConnectionManager;

    public DatabaseUserRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    public DatabaseUserRepository() {
    }

    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

    private Connection getConnection() {
        Context ctx = null;
        try {
//            ctx = new InitialContext();
//            Context envCtx = (Context) ctx.lookup("java:comp/env");
//            DataSource ds = (DataSource) envCtx.lookup("jdbc/UserPlatformDB");
//            return ds.getConnection();

            String databaseURL = "jdbc:derby:/Users/romanticolor/tools/db-derby-10.14.2.0-bin/user-platform;create=true";
            Connection connection = DriverManager.getConnection(databaseURL);

            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public boolean save(User user) {
//        return executeQuery(INSERT_USER_DML_SQL,resultSet -> {
//            return true;
//        },COMMON_EXCEPTION_HANDLER,user.getName(),user.getPassword(),user.getEmail(),user.getPhoneNumber());


//        try {
//            Context context = new InitialContext();
//            Context envCtx = (Context) context.lookup("java:comp/env");
//            DataSource ds = (DataSource) envCtx.lookup("jdbc/UserPlatformDB");
//            Connection connection = ds.getConnection();
//            connection.createStatement();
//        } catch (NamingException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }


        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("INSERT INTO users(name,password,email,phoneNumber) VALUES ('");
        stringBuffer.append(user.getName() + "','");
        stringBuffer.append(user.getPassword() + "','");
        stringBuffer.append(user.getEmail() + "','");
        stringBuffer.append(user.getPhoneNumber() + "')");

        String databaseURL = "jdbc:derby:/Users/romanticolor/tools/db-derby-10.14.2.0-bin/user-platform;create=true";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(databaseURL);
            Statement statement = connection.createStatement();
            statement.executeUpdate(stringBuffer.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean deleteById(Long userId) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User getById(Long userId) {
        return null;
    }

    @Override
    public User getByNameAndPassword(String userName, String password) {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users WHERE name=? and password=?",
                resultSet -> {
                    // TODO
                    return new User();
                }, COMMON_EXCEPTION_HANDLER, userName, password);
    }

    @Override
    public Collection<User> getAll() {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users", resultSet -> {
            // BeanInfo -> IntrospectionException
            BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
            List<User> users = new ArrayList<>();
            while (resultSet.next()) { // 如果存在并且游标滚动 // SQLException
                User user = new User();
                for (PropertyDescriptor propertyDescriptor : userBeanInfo.getPropertyDescriptors()) {
                    String fieldName = propertyDescriptor.getName();
                    Class fieldType = propertyDescriptor.getPropertyType();
                    String methodName = resultSetMethodMappings.get(fieldType);
                    // 可能存在映射关系（不过此处是相等的）
                    String columnLabel = mapColumnLabel(fieldName);
                    Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class);
                    // 通过放射调用 getXXX(String) 方法
                    Object resultValue = resultSetMethod.invoke(resultSet, columnLabel);
                    // 获取 User 类 Setter方法
                    // PropertyDescriptor ReadMethod 等于 Getter 方法
                    // PropertyDescriptor WriteMethod 等于 Setter 方法
                    Method setterMethodFromUser = propertyDescriptor.getWriteMethod();
                    // 以 id 为例，  user.setId(resultSet.getLong("id"));
                    setterMethodFromUser.invoke(user, resultValue);
                }
            }
            return users;
        }, e -> {
            // 异常处理
        });
    }

    /**
     * @param sql
     * @param function
     * @param <T>
     * @return
     */
    protected <T> T executeQuery(String sql, ThrowableFunction<ResultSet, T> function,
                                 Consumer<Throwable> exceptionHandler, Object... args) {
        Connection connection = getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                Class argType = arg.getClass();

                Class wrapperType = wrapperToPrimitive(argType);

                if (wrapperType == null) {
                    wrapperType = argType;
                }

                // Boolean -> boolean
                String methodName = preparedStatementMethodMappings.get(argType);
                Method method = PreparedStatement.class.getMethod(methodName, wrapperType);
                method.invoke(preparedStatement, i + 1, args);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            // 返回一个 POJO List -> ResultSet -> POJO List
            // ResultSet -> T
            return function.apply(resultSet);
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
        return null;
    }
}
