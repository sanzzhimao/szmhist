package dao;

import util.JdbcUtil;
import vo.ConstantItem;
import vo.Department;
import vo.RegistLevel;
import vo.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao implements IUserDao {
    Connection con=null;
    @Override
    public User findUserByName(String usern, String realn) throws SQLException {
        String sql="select ID,UserName,Password,RealName,UseType,DocTitleID,IsScheduling,DeptID,RegistLeID,DelMark\n" +
                "FROM User \n" +
                "where (UserName like ? Or RealName like ?)\n" +
                "and DelMark = 1";
        con= JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ps.setString(1,usern);
        ps.setString(2,realn);
        ResultSet rs=ps.executeQuery();
        User user=new User();
        while (rs.next()){
            user.setId(rs.getInt(1));
            user.setUserName(rs.getString(2));
            user.setPassword(rs.getString(3));
            user.setRealName(rs.getString(4));
            user.setUseTpye(rs.getInt(5));
            user.setDocTileID(rs.getInt(6));
            user.setScheduling(rs.getString(7));
            user.setDeptid(rs.getInt(8));
            user.setRegistLeID(rs.getInt(9));
            user.setDelMark(rs.getInt(10));
        }
        JdbcUtil.release(con,ps,rs);
        return user;

    }

    @Override
    public List<Department> getAllValideDepartment() throws SQLException {
        String sql="select ID,DeptCode,DeptName,DeptCategoryID,DeptType,DelMark \n" +
                "FROM Department \n" +
                "where DelMark = 1";
        con= JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ResultSet rs=ps.executeQuery();
        List<Department> departs=new ArrayList<>();
        while(rs.next()){
            Department dept=new Department();
            dept.setId(rs.getInt(1));
            dept.setDeptCode(rs.getString(2));
            dept.setDeptName(rs.getString(3));
            dept.setDeptCategoryID(rs.getInt(4));
            dept.setDeptType(rs.getInt(5));
            dept.setDelMark(rs.getInt(6));
            departs.add(dept);
        }
        JdbcUtil.release(con,ps,rs);
        return departs;
    }

    @Override
    public List<ConstantItem> getAllValideDoctorTitle() throws SQLException {
        String sql="select C1.ID, C1.ConstantCode, C1.ConstantName \n" +
                "from ConstantType C2,ConstantItem C1\n" +
                "where C1.ConstantTypeID=C2.ID\n" +
                "and ConstantTypeCode='DocTitle'\n" +
                "and C1.DelMark=1 ";
        con= JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ResultSet rs=ps.executeQuery();
        List<ConstantItem> items=new ArrayList<>();
        while(rs.next()){
            ConstantItem item=new ConstantItem();
            item.setId(rs.getInt(1));
            item.setContantCode(rs.getString(2));
            item.setConstantName(rs.getString(3));
            items.add(item);
        }
        JdbcUtil.release(con,ps,rs);
        return items;
    }

    @Override
    public List<RegistLevel> getAllValideLevel() throws SQLException {
        String sql="select id,RegistCode,RegistName,SequenceNo,RegistFee,RegistQuota,DelMark\n" +
                "from RegistLevel \n" +
                "where DelMark=1\n" +
                "order by SequenceNo";
        con= JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ResultSet rs=ps.executeQuery();
        List<RegistLevel> levels=new ArrayList<>();
        while(rs.next()){
            RegistLevel level=new RegistLevel();
            level.setId(rs.getInt(1));
            level.setRegistCode(rs.getString(2));
            level.setRegistName(rs.getString(3));
            level.setSequenceNo(rs.getInt(4));
            level.setRegistFree((double)rs.getInt(5));
            level.setRegistquota(rs.getInt(6));
            level.setDelMark(rs.getInt(7));
            levels.add(level);
        }
        JdbcUtil.release(con,ps,rs);
        return levels;
    }

    @Override
    public void addNewUser(String usern, String pass, String realn, int usert, int dectitleid, char issche, int deptid, int regisLeid, int delmark) throws SQLException {
        String sql1="SELECT count(id) \n" +
                "FROM user\n" +
                "where UserName = ?\n" +
                "and DelMark = 1";

        String sql2="INSERT INTO user(username,password,realname,usetype,doctitleid,isscheduling,deptid,registleid,delmark)\n" +
                "VALUES(?,?,?,?,?,?,?,?,?)  ";

        con=JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql1);
        ps.setString(1,usern);
        ResultSet rs=ps.executeQuery();
        int num=0;
        while(rs.next()){
            num=rs.getInt(1);
        }
        //通过id的数量判断费用是否重复
        if(num==0){  //没有这个用户则将新信息添加进去
            ps=con.prepareStatement(sql2);
            ps.setString(1,usern);
            ps.setString(2,pass);
            ps.setString(3,realn);
            ps.setInt(4,usert);
            ps.setInt(5,dectitleid);
            ps.setString(6,String.valueOf(issche));
            ps.setInt(7,deptid);
            ps.setInt(8,regisLeid);
            ps.setInt(9,delmark);

            int i=ps.executeUpdate();
            JdbcUtil.release(con,null,null);
        }else{
            JdbcUtil.release(con,null,null);
        }


    }

    @Override
    public User getUserById(int id) throws SQLException {
        String sql="select ID,UserName,Password,RealName,UseType,DocTitleID,IsScheduling,DeptID,RegistLeID,DelMark\n" +
                "FROM User \n" +
                "where ID=?\n" +
                "and DelMark = 1 ";
        con=JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ps.setInt(1,id);
        ResultSet rs=ps.executeQuery();
        User user=new User();
        while (rs.next()){
            user.setId(rs.getInt(1));
            user.setUserName(rs.getString(2));
            user.setPassword(rs.getString(3));
            user.setRealName(rs.getString(4));
            user.setUseTpye(rs.getInt(5));
            user.setDocTileID (rs.getInt(6));
            user.setScheduling(rs.getString(7));
            user.setDeptid(rs.getInt(8));
            user.setRegistLeID(rs.getInt(9));
            user.setDelMark(rs.getInt(10));
        }
        JdbcUtil.release(con,ps,rs);
        return user;
    }

    @Override
    public void updateUserByName(String usern,String pass,String realn,int usert,int dectitleid,char issche,int deptid,int regisLeid,int delmark) throws SQLException {
        String sql1="SELECT count(id) \n" +
                "FROM User \n" +
                "where UserName = ? \n" +
                "and DelMark = 1";

        String sql2 = "update user set  username=?,password=?,realname=?,usetype=?,doctitleid=?,isscheduling=?,deptid=?,registleid=?,delmark=?" +
                "      where username=?";

        con=JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql1);
        ps.setString(1,usern);
        ResultSet rs=ps.executeQuery();
        int num=0;
        while(rs.next()){
            num=rs.getInt(1);
        }
        //通过id的数量判断费用科目是否重复
        if(num==1){  //不重复则将jack添加进去
            ps=con.prepareStatement(sql2);
            ps.setString(1,usern);
            ps.setString(2,pass);
            ps.setString(3,realn);
            ps.setInt(4,usert);
            ps.setInt(5,dectitleid);
            ps.setString(6,String.valueOf(issche));
            ps.setInt(7,deptid);
            ps.setInt(8,regisLeid);
            ps.setInt(9,delmark);
            ps.setString(10,usern);
            int i=ps.executeUpdate();
            JdbcUtil.release(con,null,null);
        }else{
            JdbcUtil.release(con,null,null);
        }

    }

    @Override
    public void deleteUserByID(int id) throws SQLException {
        String sql="update  user \n" +
                "set DelMark = 0 \n" +
                "WHERE id = ?";
        con= JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ps.setInt(1,id);
        int i=ps.executeUpdate();
        JdbcUtil.release(con,null,null);

    }

    @Override
    public User findUserByNamePass(String username, String pass) throws SQLException {
        String sql="select ID,UserName,Password,RealName,UseType,DocTitleID,IsScheduling,DeptID,RegistLeID,DelMark\n" +
                "FROM User \n" +
                "where UserName = ? " +
                "and Password = ?" +
                "and DelMark = 1";
        con= JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ps.setString(1,username);
        ps.setString(2,pass);
        ResultSet rs=ps.executeQuery();
        User user=new User();
        while (rs.next()){
            user.setId(rs.getInt(1));
            user.setUserName(rs.getString(2));
            user.setPassword(rs.getString(3));
            user.setRealName(rs.getString(4));
            user.setUseTpye(rs.getInt(5));
            user.setDocTileID(rs.getInt(6));
            user.setScheduling(rs.getString(7));
            user.setDeptid(rs.getInt(8));
            user.setRegistLeID(rs.getInt(9));
            user.setDelMark(rs.getInt(10));
        }
        JdbcUtil.release(con,ps,rs);
        return user;
    }
}
