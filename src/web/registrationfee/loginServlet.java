package web.registrationfee;

import net.sf.json.JSONObject;
import vo.LoginMessage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns ="/login/logins")
public class loginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       System.out.println("dangerous be care for dingming hui gen luo jia qibiao bai");

       if(request.getParameter("account").equals("root")&&request.getParameter("password").equals("root")){
           System.out.println("进入登录");
           LoginMessage ms=new LoginMessage();
           ms.setTime(2);
           ms.setStr("this is www ding min");
           JSONObject json=JSONObject.fromObject(ms);
           String str=json.toString();
           PrintWriter pw=response.getWriter();
           pw.println(str);
           pw.flush();

       }


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}


