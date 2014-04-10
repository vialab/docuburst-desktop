package ca.utoronto.cs.docuburst.data.treecut;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;


/**
 * Implementation of Node with very reduced capabilities,
 * only to suit the needs of {@link MDLTreeCut}.
 * 
 * @author Rafael Veras
 *
 */
public class FakeNode implements Node {

    private int childCount = 0;
    
    HashMap<String, Object> fields;
    
    public FakeNode() {
        fields = new HashMap<String, Object>();
    }

    @Override
    public void set(String field, Object value) {
        fields.put(field, value);
    }

    @Override
    public int getInt(String field) {
        return (Integer)fields.get(field);
    }
    
    @Override
    public void setInt(String field, int val) {
        fields.put(field, val);
    }
    
    @Override
    public int getChildCount() {
        return childCount;
    }
    
    public void setChildCount(int val) {
        childCount = val;
    }
    
    
    @Override
    public Schema getSchema() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Table getTable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRow() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Class getColumnType(String field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class getColumnType(int col) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getColumnIndex(String field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getColumnCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getColumnName(int col) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canGet(String field, Class type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSet(String field, Class type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object get(String field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object get(int col) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void set(int col, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getDefault(String field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void revertToDefault(String field) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canGetInt(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSetInt(String field) {
        return false;
    }

    @Override
    public int getInt(int col) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setInt(int col, int val) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canGetLong(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSetLong(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getLong(String field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setLong(String field, long val) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getLong(int col) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setLong(int col, long val) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canGetFloat(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSetFloat(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public float getFloat(String field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFloat(String field, float val) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getFloat(int col) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFloat(int col, float val) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canGetDouble(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSetDouble(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getDouble(String field) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setDouble(String field, double val) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getDouble(int col) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setDouble(int col, double val) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canGetBoolean(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSetBoolean(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getBoolean(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setBoolean(String field, boolean val) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getBoolean(int col) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setBoolean(int col, boolean val) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canGetString(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSetString(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getString(String field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setString(String field, String val) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getString(int col) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setString(int col, String val) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canGetDate(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSetDate(String field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Date getDate(String field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDate(String field, Date val) {
        // TODO Auto-generated method stub

    }

    @Override
    public Date getDate(int col) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDate(int col, Date val) {
        // TODO Auto-generated method stub

    }

    @Override
    public Graph getGraph() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getInDegree() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getOutDegree() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDegree() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Iterator inEdges() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator outEdges() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator edges() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator inNeighbors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator outNeighbors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator neighbors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Edge getParentEdge() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getDepth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getChildIndex(Node child) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Node getChild(int idx) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getFirstChild() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getLastChild() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getPreviousSibling() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getNextSibling() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator children() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator childEdges() {
        // TODO Auto-generated method stub
        return null;
    }

}
