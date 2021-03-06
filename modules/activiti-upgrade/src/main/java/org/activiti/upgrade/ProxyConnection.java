/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.upgrade;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;


public class ProxyConnection implements Connection {

  Connection connection;
  ProxyDriver proxyDriver;

  public ProxyConnection(Connection connection, ProxyDriver proxyDriver) {
    this.connection = connection;
    this.proxyDriver = proxyDriver;
  }

  private Statement wrapStatement(Statement statement) {
    return new ProxyStatement(statement, this);
  }


  public boolean isWrapperFor(Class< ? > iface) throws SQLException {
    return connection.isWrapperFor(iface);
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    return connection.unwrap(iface);
  }

  public void clearWarnings() throws SQLException {
    connection.clearWarnings();
  }

  public void close() throws SQLException {
    connection.close();
  }

  public void commit() throws SQLException {
    connection.commit();
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return connection.createArrayOf(typeName, elements);
  }

  public Blob createBlob() throws SQLException {
    return connection.createBlob();
  }

  public Clob createClob() throws SQLException {
    return connection.createClob();
  }

  public NClob createNClob() throws SQLException {
    return connection.createNClob();
  }

  public SQLXML createSQLXML() throws SQLException {
    return connection.createSQLXML();
  }

  public Statement createStatement() throws SQLException {
    return wrapStatement(connection.createStatement());
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    return wrapStatement(connection.createStatement(resultSetType, resultSetConcurrency));
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return wrapStatement(connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
  }

  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return connection.createStruct(typeName, attributes);
  }

  public boolean getAutoCommit() throws SQLException {
    return connection.getAutoCommit();
  }

  public String getCatalog() throws SQLException {
    return connection.getCatalog();
  }

  public Properties getClientInfo() throws SQLException {
    return connection.getClientInfo();
  }

  public String getClientInfo(String name) throws SQLException {
    return connection.getClientInfo(name);
  }

  public int getHoldability() throws SQLException {
    return connection.getHoldability();
  }

  public DatabaseMetaData getMetaData() throws SQLException {
    return connection.getMetaData();
  }

  public int getTransactionIsolation() throws SQLException {
    return connection.getTransactionIsolation();
  }

  public Map<String, Class< ? >> getTypeMap() throws SQLException {
    return connection.getTypeMap();
  }

  public SQLWarning getWarnings() throws SQLException {
    return connection.getWarnings();
  }

  public boolean isClosed() throws SQLException {
    return connection.isClosed();
  }

  public boolean isReadOnly() throws SQLException {
    return connection.isReadOnly();
  }

  public boolean isValid(int timeout) throws SQLException {
    return connection.isValid(timeout);
  }

  public String nativeSQL(String sql) throws SQLException {
    return connection.nativeSQL(sql);
  }

  public CallableStatement prepareCall(String sql) throws SQLException {
    return connection.prepareCall(sql);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return new ProxyStatement(connection.prepareStatement(sql), sql, this);
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return new ProxyStatement(connection.prepareStatement(sql, autoGeneratedKeys), sql, this);
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return new ProxyStatement(connection.prepareStatement(sql, columnIndexes), sql, this);
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return new ProxyStatement(connection.prepareStatement(sql, columnNames), sql, this);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    return new ProxyStatement(connection.prepareStatement(sql, resultSetType, resultSetConcurrency), sql, this);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return new ProxyStatement(connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql, this);
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    connection.releaseSavepoint(savepoint);
  }

  public void rollback() throws SQLException {
    connection.rollback();
  }

  public void rollback(Savepoint savepoint) throws SQLException {
    connection.rollback(savepoint);
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException {
    connection.setAutoCommit(autoCommit);
  }

  public void setCatalog(String catalog) throws SQLException {
    connection.setCatalog(catalog);
  }

  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    connection.setClientInfo(properties);
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    connection.setClientInfo(name, value);
  }

  public void setHoldability(int holdability) throws SQLException {
    connection.setHoldability(holdability);
  }

  public void setReadOnly(boolean readOnly) throws SQLException {
    connection.setReadOnly(readOnly);
  }

  public Savepoint setSavepoint() throws SQLException {
    return connection.setSavepoint();
  }

  public Savepoint setSavepoint(String name) throws SQLException {
    return connection.setSavepoint(name);
  }

  public void setTransactionIsolation(int level) throws SQLException {
    connection.setTransactionIsolation(level);
  }

  public void setTypeMap(Map<String, Class< ? >> map) throws SQLException {
    connection.setTypeMap(map);
  }

  public void setSchema(String schema) throws SQLException {
    throw new RuntimeException("buzz");
  }

  public String getSchema() throws SQLException {
    throw new RuntimeException("buzz");
  }

  public void abort(Executor executor) throws SQLException {
    throw new RuntimeException("buzz");
  }

  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    throw new RuntimeException("buzz");
  }

  public int getNetworkTimeout() throws SQLException {
    throw new RuntimeException("buzz");
  }
}
