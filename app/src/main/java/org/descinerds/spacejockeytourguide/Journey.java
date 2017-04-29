package org.descinerds.spacejockeytourguide;

import java.io.Serializable;
import java.util.List;

public class Journey implements Serializable {
    public String name;
    public List<String> tour;
    public List<String> adventure;

    public Journey() { }
}
