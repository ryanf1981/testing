package com.cTimers;

import com.cTimers.ui.cTimersPanelPrimary;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.events.UserJoin;
import net.runelite.client.party.events.UserPart;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import com.cTimers.rooms.*;
import com.cTimers.utility.cLogger;
import net.runelite.client.plugins.devtools.DevToolsPlugin;
import net.runelite.client.plugins.specialcounter.SpecialCounterUpdate;
import net.runelite.client.plugins.specialcounter.SpecialWeapon;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.cTimers.constants.LogID.*;


@Slf4j
@PluginDescriptor(
        name = "cTimers",
        description = "Timers and statistics for Theatre of Blood",
        tags = {"timers", "tob", "tracker", "time", "theatre"}
)
public class cTimersPlugin extends Plugin
{
    private cTimersPanelPrimary timersPanelPrimary;

    private NavigationButton navButtonPrimary;
    private cLogger clog;

    private boolean partyIntact = false;

    @Inject
    private cTimersConfig config;
    @Provides
    cTimersConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(cTimersConfig.class);
    }
    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ClientThread clientThread;

    @Inject
    private PartyService party;

    @Inject
    private Client client;

    private cLobby lobby;
    private cMaiden maiden;
    private cBloat bloat;
    private cNylo nylo;
    private cSotetseg sote;
    private cXarpus xarpus;
    private cVerzik verzik;


    private final int LOBBY_REGION = 14642;
    private final int MAIDEN_REGION = 12613;
    private final int BLOAT_REGION = 13125;
    private final int NYLO_REGION = 13122;
    private final int SOTETSEG_REGION = 13123;
    private final int SOTETSEG_UNDER_REGION = 13379;
    private final int XARPUS_REGION = 12612;
    private final int VERZIK_REGION = 12611;

    private final int[] theatreIDs = {8360, 8361, 8362, 8363, 8364, 8365, 8359, 8355, 8356, 8357, 8387, 8388, 8338, 8339, 8340, 8341, 8369, 8370, 8371, 8372, 8373, 8374, 8375};

    private boolean inTheatre;
    private boolean wasInTheatre;
    private cRoom currentRoom;
    int deferredTick;
    private boolean flagPlayer = false;
    private int mode = -1;
    private final int REGULAR_TOB = 0;
    private final int STORY_TOB = 1;
    private final int HARD_TOB = 2;
    private ArrayList<String> currentPlayers;
    private boolean checkDefer = false;
    public static int scale = -1;

    @Inject
    private PluginManager pluginManager;

    @Inject
    private EventBus eventBus;


    @Override
    protected void shutDown()
    {
        partyIntact = false;
        clientToolbar.removeNavigation(navButtonPrimary);
    }

    @Override
    protected void startUp() throws Exception
    {
        super.startUp();

        timersPanelPrimary = injector.getInstance(cTimersPanelPrimary.class);
        partyIntact = false;


        final BufferedImage icon = ImageUtil.loadImageResource(DevToolsPlugin.class, "devtools_icon.png");
        navButtonPrimary = NavigationButton.builder().tooltip("cTimersPrimary").icon(icon).priority(10).panel(timersPanelPrimary).build();

        clientToolbar.addNavigation(navButtonPrimary);

        clog = new cLogger(client, config);
        lobby = new cLobby(client, clog, config);
        maiden = new cMaiden(client, clog, config);
        bloat = new cBloat(client, clog, config);
        nylo = new cNylo(client, clog, config);
        sote = new cSotetseg(client, clog, config);
        xarpus = new cXarpus(client, clog, config);
        verzik = new cVerzik(client, clog, config);
        inTheatre = false;
        wasInTheatre = false;
        deferredTick = 0;
        currentPlayers = new ArrayList<>();
    }

    private int getRoom() {
        if (inRegion(LOBBY_REGION))
            return -1;
        else if (inRegion(MAIDEN_REGION))
            return 0;
        else if (inRegion(BLOAT_REGION))
            return 1;
        else if (inRegion(NYLO_REGION))
            return 2;
        else if (inRegion(SOTETSEG_REGION) || inRegion(SOTETSEG_UNDER_REGION))
            return 3;
        else if (inRegion(XARPUS_REGION))
            return 4;
        else if (inRegion(VERZIK_REGION))
            return 5;
        return -1;
    }

    private void updateRoom()
    {
        cRoom previous = currentRoom;
        int currentRegion = getRoom();
        boolean activeState = false;
        if(inRegion(LOBBY_REGION))
        {
            currentRoom = lobby;
        }
        else if (previous == lobby && !inRegion(MAIDEN_REGION)) //TODO faulty logic
        {
            deferredTick = client.getTickCount()+2;
            clog.write(ENTERED_TOB);
            clog.write(SPECTATE);
            clog.write(LATE_START, String.valueOf(currentRegion));
        }
        if(inRegion(MAIDEN_REGION))
        {
            if(previous != maiden)
            {
                currentRoom = maiden;
                enteredMaiden(previous);
            }
            activeState = true;
        }
        else if(inRegion(BLOAT_REGION))
        {
            if(previous != bloat)
            {
                currentRoom = bloat;
                enteredBloat(previous);
            }
            activeState = true;
        }
        else if(inRegion(NYLO_REGION))
        {
            if(previous != nylo)
            {
                currentRoom = nylo;
                enteredNylo(previous);
            }
            activeState = true;
        }
        else if(inRegion(SOTETSEG_REGION, SOTETSEG_UNDER_REGION))
        {
            if(previous != sote)
            {
                currentRoom = sote;
                enteredSote(previous);
            }
            activeState = true;
        }
        else if(inRegion(XARPUS_REGION))
        {
            if(previous != xarpus)
            {
                currentRoom = xarpus;
                enteredXarpus(previous);
            }
            activeState = true;
        }
        else if(inRegion(VERZIK_REGION))
        {
            if(previous != verzik)
            {
                currentRoom = verzik;
                enteredVerzik(previous);
            }
            activeState = true;
        }
        inTheatre = activeState;
    }

    private void enteredMaiden(cRoom old)
    {
        clog.write(ENTERED_TOB);
        deferredTick = client.getTickCount()+2;
        maiden.reset();
    }

    private void enteredBloat(cRoom old)
    {
        clog.write(ENTERED_NEW_TOB_REGION, "1");
        maiden.reset();
        bloat.reset();
    }

    private void enteredNylo(cRoom old)
    {
        clog.write(ENTERED_NEW_TOB_REGION, "2");
        bloat.reset();
        nylo.reset();
    }

    private void enteredSote(cRoom old)
    {
        clog.write(ENTERED_NEW_TOB_REGION, "3");
        nylo.reset();
        sote.reset();
    }

    private void enteredXarpus(cRoom old)
    {
        clog.write(ENTERED_NEW_TOB_REGION, "4");
        sote.reset();
        xarpus.reset();
    }

    private void enteredVerzik(cRoom old)
    {
        clog.write(ENTERED_NEW_TOB_REGION, "5");
        xarpus.reset();
        verzik.reset();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if(event.getGameState() == GameState.LOGGED_IN)
        {
            checkDefer = true;
        }
    }

    @Subscribe
    public void onSpecialCounterUpdate(SpecialCounterUpdate event)
    {
        if(inTheatre)
        {
            String name = party.getMemberById(event.getMemberId()).getDisplayName();
            if (name == null) {
                return;
            }
            boolean playerInRaid = false;
            for(String player : currentPlayers)
            {
                if (name.equals(player.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32)))) {
                    playerInRaid = true;
                    break;
                }
            }
            if(playerInRaid)
            {
                if(event.getWeapon().equals(SpecialWeapon.BANDOS_GODSWORD))
                {
                    clog.write(3, name, ""+event.getHit());
                }
                if(event.getWeapon().equals(SpecialWeapon.DRAGON_WARHAMMER))
                {
                    clog.write(2, name);
                }
            }
        }
    }

    private String cleanString(String s1)
    {
        return s1.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
    }

    private boolean isPartyComplete()
    {
        if(currentPlayers.size() > party.getMembers().size())
        {
            return false;
        }
        for(String raidPlayer : currentPlayers)
        {
            boolean currentPlayerMatched = false;
            for(PartyMember partyPlayer : party.getMembers())
            {
                if(cleanString(raidPlayer).equals(partyPlayer.getDisplayName()))
                {
                    currentPlayerMatched = true;
                }
            }
            if(!currentPlayerMatched)
            {
                return false;
            }
        }
        return true;
    }

    private void checkPartyUpdate()
    {
        checkPartyUpdate(false);
    }
    private void checkPartyUpdate(boolean preMaiden)
    {
        if(inTheatre)
        {
            if(partyIntact)
            {
                if (!isPartyComplete())
                {
                    partyIntact = false;
                    clog.write(101);
                }
            }
            else
            {
                if(isPartyComplete())
                {
                    partyIntact = true;
                    clog.write(100);

                }
            }
        }

    }

    @Subscribe
    public void onPartyChanged(final PartyChanged party)
    {
        checkPartyUpdate();
    }

    @Subscribe
    public void onUserPart(final UserPart event)
    {
        checkPartyUpdate();
    }

    @Subscribe
    public void onUserJoin(final UserJoin event)
    {
        checkPartyUpdate();
    }

    @Subscribe
    public void onGameTick(GameTick event) throws PluginInstantiationException
    {
        updateRoom();
        if(inTheatre)
        {
            wasInTheatre = true;
            currentRoom.updateGameTick(event);

            if(client.getTickCount() == deferredTick)
            {
                String[] players = {"", "", "", "", ""};
                for(int i = 330; i < 335; i++)
                {
                    if(client.getVarcStrValue(i) != null && !client.getVarcStrValue(i).equals(""))
                    {
                        players[i-330] = Text.escapeJagex(client.getVarcStrValue(i));
                    }
                }
                for(String s : players)
                {
                    if(!s.equals(""))
                    {
                        currentPlayers.add(s);
                    }
                }
                checkPartyUpdate(true);
                boolean flag = false;
                for(String p : players)
                {
                    if(p.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32)).equals(client.getLocalPlayer().getName().replaceAll(String.valueOf((char) 160), String.valueOf((char) 32))))
                    {
                        flag = true;
                    }
                }
                deferredTick = 0;
                if(!flag)
                {
                    clog.write(SPECTATE);
                }
                clog.write(PARTY_MEMBERS, players[0], players[1], players[2], players[3], players[4]);
                maiden.setScale(Arrays.stream(players).filter(x -> !x.equals("")).collect(Collectors.toList()).size());
                scale = currentPlayers.size();
                //TODO better way of doing this
            }
        }
        else
        {
            if(wasInTheatre)
            {
                leftRaid();
                wasInTheatre = false;
            }
        }
    }

    public void leftRaid()
    {
        partyIntact = false;
        currentPlayers.clear();
        clog.write(LEFT_TOB); //todo add region
        currentRoom = null;
    }

    @Subscribe
    public void onActorDeath(ActorDeath event)
    {
        if(inTheatre)
        {
            Actor a = event.getActor();
            if(a instanceof Player)
            {
                clog.write(PLAYER_DIED, event.getActor().getName());
            }
        }
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event)
    {
        if(inTheatre)
        {
            currentRoom.updateGraphicChanged(event);
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event)
    {
        if(inTheatre)
        {
            currentRoom.updateProjectileMoved(event);
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {
        if(inTheatre)
        {
            currentRoom.updateAnimationChanged(event);
        }
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        if(inTheatre)
        {
            currentRoom.updateInteractingChanged(event);
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event)
    {
        if(inTheatre)
        {
            currentRoom.handleNPCChanged(event.getNpc().getId());
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        switch(event.getNpc().getId())
        {
            case 8360:
            case 8361:
            case 8362:
            case 8363:
            case 8364:
            case 8365://TODO remove later if doesn't work
            {
                maiden.updateNpcSpawned(event);
                clog.write(12); //TODO really this should happen in maiden class
            }
                break;
            case 8366:
                maiden.updateNpcSpawned(event);
                break;
            case 8359:
                bloat.updateNpcSpawned(event);
                break;
            case 8355:
            case 8356:
            case 8357:
            case 8342:
            case 8343:
            case 8344:
            case 8345:
            case 8346:
            case 8347:
            case 8351:
            case 8352:
            case 8353:
            case 10794:
            case 10795:
            case 10796:
            case 10791:
            case 10792:
            case 10793:
            case 10803:
            case 10804:
            case 10805:
            case 10806:
                nylo.updateNpcSpawned(event);
                break;
            case 8388:
            case 8387:
                sote.updateNpcSpawned(event);
                break;
            case 8338:
            case 8339:
            case 8340:
            case 8341:
                clog.write(50);
                xarpus.updateNpcSpawned(event);
                break;
            case 8369:
            case 8370:
            case 8371:
            case 8372:
            case 8373:
            case 8374:
            case 8375:
                verzik.updateNpcSpawned(event);
                break;
            case 8367:
            {
                clog.write(BLOOD_SPAWNED, ""+getRoomTime());
                maiden.updateNpcSpawned(event);
            }
                break;
            default:
                if(currentRoom != null)
                {
                    currentRoom.updateNpcSpawned(event);
                }
                break;
        }
    }

    public int getRoomTime()
    {
        //TODO caps???
        return 0;
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        if(inTheatre)
        {
            currentRoom.updateNpcDespawned(event);
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event)
    {
        if(inTheatre)
        {
            currentRoom.updateHitsplatApplied(event);
        }
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event)
    {
        if(currentRoom instanceof cXarpus)
        {
            xarpus.updateOverheadText(event);
        }
    }

    public boolean inRegion(int... regions)
    {
        if (client.getMapRegions() != null)
        {
            for (int currentRegion : client.getMapRegions())
            {
                for (int region : regions)
                {
                    if (currentRegion == region)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
