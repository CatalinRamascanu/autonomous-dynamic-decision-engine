package com.adobe.primetime.adde.webserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public class ControllerException extends Exception {

    public ControllerException(String msg) {
        super(msg);
    }
}
