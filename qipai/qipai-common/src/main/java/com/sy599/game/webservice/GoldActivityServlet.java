package com.sy599.game.webservice;

import com.sy.mainland.util.UrlParamUtil;
import com.sy599.game.character.Player;
import com.sy599.game.manager.PlayerManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class GoldActivityServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> paramsMap = UrlParamUtil.getParameters(request);
        String type = paramsMap.get("type");
        if("sendUser".equals(type)){
            Long aimId = Long.parseLong(paramsMap.get("aimId"));
            Player aimPlayer=null;
            if(aimId!=null)
                aimPlayer= PlayerManager.getInstance().getPlayer(aimId);
            if(aimPlayer==null)
                return;

            String method = paramsMap.get("method");
            switch (method){
                case "ActiveQueQiao_acceptTeamateChangePlayNum" :
                    aimPlayer.getAqq().acceptTeamateChangePlayNum(paramsMap);
                    break;
                case "ActiveQueQiao_acceptInvite" :
                    aimPlayer.getAqq().acceptInvite();
                    break;
                case "ActiveQueQiao_teamSuccess" :
                    aimPlayer.getAqq().teamSuccess(paramsMap);
                    break;
            }
        }

    }
}
