package com.TheatreTracker.panelcomponents;

import com.TheatreTracker.utility.PlayerDidAttack;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

@Slf4j
public class LiveChartFrame extends BaseFrame
{
    ChartPanel maidenPanel;
    ChartPanel bloatPanel;
    ChartPanel nyloPanel;
    ChartPanel sotetsegPanel;
    ChartPanel xarpPanel;
    ChartPanel verzPanel;
    JScrollPane maidenScroll;
    JScrollPane bloatScroll;
    JScrollPane nyloScroll;
    JScrollPane sotetsegScroll;
    JScrollPane xarpusScroll;
    JScrollPane verzikScroll;
    public JTabbedPane tabbedPane;

    public LiveChartFrame()
    {
        maidenPanel = new ChartPanel("Maiden", true);
        bloatPanel = new ChartPanel("Bloat", true);
        nyloPanel = new ChartPanel("Nylocas", true);
        sotetsegPanel = new ChartPanel("Sotetseg", true);
        xarpPanel = new ChartPanel("Xarpus", true);
        verzPanel = new ChartPanel("Verzik", true);

        maidenScroll = new JScrollPane(maidenPanel);
         bloatScroll = new JScrollPane(bloatPanel);
         nyloScroll = new JScrollPane(nyloPanel);
         sotetsegScroll = new JScrollPane(sotetsegPanel);
         xarpusScroll = new JScrollPane(xarpPanel);
         verzikScroll = new JScrollPane(verzPanel);


        tabbedPane = new JTabbedPane();

        tabbedPane.add("Maiden", maidenScroll);
        tabbedPane.add("Bloat", bloatScroll);
        tabbedPane.add("Nylocas", nyloScroll);
        tabbedPane.add("Sotetseg", sotetsegScroll);
        tabbedPane.add("Xarpus", xarpusScroll);
        tabbedPane.add("Verzik", verzikScroll);

        add(tabbedPane);
        pack();
    }

    public ChartPanel getPanel(String room)
    {
        switch(room)
        {
            case "Maiden":
                return maidenPanel;
            case "Bloat":
                return bloatPanel;
            case "Nylocas":
                return nyloPanel;
            case "Sotetseg":
                return sotetsegPanel;
            case "Xarpus":
                return xarpPanel;
            case "Verzik":
                return verzPanel;
        }
        return new ChartPanel("", true);
    }

    public void incrementTick(String room)
    {
        getPanel(room).incrementTick();
        if(getPanel(room).endTick % 50 == 0)
        {
            maidenScroll.getViewport().setViewPosition(new Point(maidenPanel.getViewRect().x, maidenPanel.getViewRect().y));
            bloatScroll.getViewport().setViewPosition(new Point(bloatPanel.getViewRect().x, bloatPanel.getViewRect().y));
            nyloScroll.getViewport().setViewPosition(new Point(nyloPanel.getViewRect().x, nyloPanel.getViewRect().y));
            sotetsegScroll.getViewport().setViewPosition(new Point(sotetsegPanel.getViewRect().x, sotetsegPanel.getViewRect().y));
            xarpusScroll.getViewport().setViewPosition(new Point(xarpPanel.getViewRect().x, xarpPanel.getViewRect().y));
            verzikScroll.getViewport().setViewPosition(new Point(verzPanel.getViewRect().x, verzPanel.getViewRect().y));
        }
    }

    public void addAttack(PlayerDidAttack attack, String room)
    {
        getPanel(room).addLiveAttack(attack);
    }

    public void addMaidenLine(int value, String description)
    {
        maidenPanel.addLine(value, description);
    }

    public void addBloatLine(int value, String description)
    {
        bloatPanel.addLine(value, description);
    }

    public void addNyloLine(int value, String description)
    {
        nyloPanel.addLine(value, description);
    }

    public void addSoteLine(int value, String description)
    {
        sotetsegPanel.addLine(value, description);
    }

    public void addXarpLine(int value, String description)
    {
        xarpPanel.addLine(value, description);
    }

    public void addVerzikLine(int value, String description)
    {
        verzPanel.addLine(value, description);
    }

    public void setMaidenFinished()
    {
        maidenPanel.setRoomFinished();
    }
    public void setBloatFinished()
    {
        bloatPanel.setRoomFinished();
    }
    public void setNyloFinished()
    {
        nyloPanel.setRoomFinished();
    }
    public void setSoteFinished()
    {
        sotetsegPanel.setRoomFinished();
    }
    public void setXarpFinished()
    {
        xarpPanel.setRoomFinished();
    }
    public void setVerzFinished()
    {
        verzPanel.setRoomFinished();
    }

    public void resetAll()
    {
        maidenPanel.resetGraph();
        bloatPanel.resetGraph();
        nyloPanel.resetGraph();
        sotetsegPanel.resetGraph();
        xarpPanel.resetGraph();
        verzPanel.resetGraph();

        maidenScroll.getVerticalScrollBar().setValue(0);
        bloatScroll.getVerticalScrollBar().setValue(0);
        nyloScroll.getVerticalScrollBar().setValue(0);
        sotetsegScroll.getVerticalScrollBar().setValue(0);
        xarpusScroll.getVerticalScrollBar().setValue(0);
        verzikScroll.getVerticalScrollBar().setValue(0);
    }
    public void setPlayers(ArrayList<String> players)
    {
        ArrayList<String> cleanedPlayers = new ArrayList<>();
        for(String s : players)
        {
            cleanedPlayers.add(s.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32)));
        }
        maidenPanel.setPlayers(cleanedPlayers);
        bloatPanel.setPlayers(cleanedPlayers);
        nyloPanel.setPlayers(cleanedPlayers);
        sotetsegPanel.setPlayers(cleanedPlayers);
        xarpPanel.setPlayers(cleanedPlayers);
        verzPanel.setPlayers(cleanedPlayers);
    }
}
