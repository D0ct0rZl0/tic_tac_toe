package ru.bagautdinov;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bagautdinov.models.Game;
import ru.bagautdinov.models.Player;
import ru.bagautdinov.repository.PlayerRepository;
import ru.bagautdinov.utils.CommadnsHandler;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;


@Component
@ServerEndpoint(value = "/server", encoders = {MessageEncoder.class}, decoders = {MessageDecoder.class})
public class ServerEndPoint {

    @Autowired
    PlayerRepository pr;
    @Autowired
    CommadnsHandler commadnsHandler;

    static Set<Session> detailUsers = Collections.synchronizedSet(new HashSet<Session>());

    @OnOpen
    public void handleOpen(Session session){
        System.out.println(format("%s joined ", session.getId()));
        detailUsers.add(session);
    }

    @OnMessage
    public void handleMessage(Message incomingMessage, Session session) throws IOException, EncodeException {
        Game currentGame = (Game) session.getUserProperties().get("currentGame");
        Player player = (Player) session.getUserProperties().get("player");
        if (incomingMessage==null)return;
        if(player==null) System.out.println("hui");
        if(player==null){
            if(!pr.existsByName(incomingMessage.getUsername()))return;
            player= pr.findByName(incomingMessage.getUsername());
            session.getUserProperties().put("player",player);
        }
        commadnsHandler.proceedCommand(incomingMessage.getMessage(), currentGame, player,session);



    }

    @OnClose
    public void handleClose(Session session){
        detailUsers.remove(session);
    }

    @OnError
    public void handleError(Throwable t){
        t.printStackTrace();
    }
}

