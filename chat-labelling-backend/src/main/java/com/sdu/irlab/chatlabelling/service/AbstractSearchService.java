package com.sdu.irlab.chatlabelling.service;

import org.springframework.stereotype.Service;

import java.util.List;

public abstract class AbstractSearchService {
    public abstract String searchStates(List<String> states);
}
