/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketconnection;

import java.net.ConnectException;

/**
 *
 * @author julien
 */
public enum RC{
   success,
   failed,
   disconnected,
   connected,
   unknownHostException,
   IOException,
   ClassNotFoundException,
   ConnectException,
   debug

}