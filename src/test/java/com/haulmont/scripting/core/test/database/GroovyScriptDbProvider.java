package com.haulmont.scripting.core.test.database;

import com.haulmont.scripting.repository.provider.ScriptNotFoundException;
import com.haulmont.scripting.repository.provider.ScriptProvider;
import org.hsqldb.jdbc.JDBCDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component("groovyDbProvider")
public class GroovyScriptDbProvider implements ScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptDbProvider.class);

    @Autowired
    private JDBCDataSource dataSource;

    private String getScriptTextbyName(String name) throws SQLException {
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
    public ScriptSource getScript(Method method) {
        Class<?> scriptRepositoryClass = method.getDeclaringClass();
        String methodName = method.getName();
        String scriptName = scriptRepositoryClass.getSimpleName() + "." + methodName;
        String script = null;
        try {
            script = getScriptTextbyName(scriptName);
            log.trace("Scripted method name: {} text: {}", scriptName, script);
            Resource r = new ByteArrayResource(script.getBytes(StandardCharsets.UTF_8));
            return new ResourceScriptSource(r);
        } catch (SQLException e) {
            throw new ScriptNotFoundException(e);
        }
    }
}
