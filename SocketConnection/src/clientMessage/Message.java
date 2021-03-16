/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientMessage;

import clientMessage.MessageData.IMessage;
import clientMessage.messageAction.IMessageAction;
import socketconnection.Login;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;


/**
 *
 * @author julien
 */
public abstract class Message implements Serializable, IMessage {

	private IMessageAction action;
	private final static Random RANDOM = new Random();
	final private int id;
	final private Timestamp timestamp;
	final private Login login;
	
	public Message(Login login) {
		this.id = RANDOM.nextInt();
		this.timestamp = Timestamp.from(Instant.now());
		this.login = login;
	}

	@Override
	public int getId() {
		return id;
	}

	public Login getLogin(){
		return login;

	}

	@Override
	public Timestamp getTimestamp() {
		return timestamp;
	}

	@Override
	public IMessageAction getAction() {
		return action;
	}

	@Override
	public void setAction(IMessageAction action) {
		this.action = action;
	}
	public void activateAction(){
		action.action();
	
	
	}
	
}
