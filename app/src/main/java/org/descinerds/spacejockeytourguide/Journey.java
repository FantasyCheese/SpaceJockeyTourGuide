package org.descinerds.spacejockeytourguide;

import java.io.Serializable;
import java.util.List;

public class Journey implements Serializable {
    public String name;
    public List<Element> tour;
    public List<Element> adventure;

    public Journey() { }
}
