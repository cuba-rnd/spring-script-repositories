package com.company.rnd.scriptrepo.core.test.database;

import com.company.rnd.scriptrepo.repository.factory.ScriptProvider;
import org.hsqldb.jdbc.JDBCDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component("groovyDbProvider")
public class GroovyScriptDbProvider implements ScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptDbProvider.class);

    @Autowired
    private JDBCDataSource dataSource;

    private String getScriptTextbyName(String name) throws Exception{
        String result = "";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("select source_text from persistent_script where name = ?")){
            st.setString(1, name);

            ResultSet rs = st.executeQuery();
            if (rs.next()){
                result = rs.getString(1);
            }
            rs.close();
        }
        return result;
    }

    @Override
    public String getScript(Method method) {
        Class<?> scriptRepositoryClass = method.getDeclaringClass();
        String methodName = method.getName();
        String scriptName = scriptRepositoryClass.getSimpleName() + "." + methodName;
        String script = null;
        try {
            script = getScriptTextbyName(scriptName);
            log.trace("Scripted method name: {} text: {}", scriptName, script);
            return script;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot fetch data from the database", e);
        }
    }
}
