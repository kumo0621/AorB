package com.github.kumo0621.aorb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public final class AorB extends JavaPlugin implements org.bukkit.event.Listener {

    private Map<String, String> voteResults;
    private boolean votingInProgress;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        voteResults = new HashMap<>();
        votingInProgress = false;

        // スコアボードの設定
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Objective objective = scoreboard.getObjective("VoteTab");

        if (objective == null) {
            objective = scoreboard.registerNewObjective("VoteTab", "dummy", ChatColor.BOLD + "投票結果");
            objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void updateTab(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(DisplaySlot.PLAYER_LIST);

        if (objective != null) {
            for (String playerName : scoreboard.getEntries()) {
                scoreboard.resetScores(playerName);
            }

            for (Map.Entry<String, String> entry : voteResults.entrySet()) {
                String playerName = entry.getKey();
                String vote = entry.getValue();

                Score score = objective.getScore(playerName);
                if (vote.equalsIgnoreCase("a")) {
                    score.setScore(4); // Aに投票したことを示すスコア
                } else if (vote.equalsIgnoreCase("b")) {
                    score.setScore(4); // Bに投票したことを示すスコア
                } else {
                    score.setScore(0); // 未投票を示すスコア
                }
            }
        }
    }

    private void displayVotedPlayers() {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Objective objective = scoreboard.getObjective("VoteTab");

        if (objective != null) {
            for (Map.Entry<String, String> entry : voteResults.entrySet()) {
                String playerName = entry.getKey();
                String vote = entry.getValue();

                Score score = objective.getScore(playerName);
                if (vote.equalsIgnoreCase("a")) {
                    score.setScore(1); // Aに投票したことを示すスコア
                } else if (vote.equalsIgnoreCase("b")) {
                    score.setScore(2); // Bに投票したことを示すスコア
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("a")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String name = player.getName();

                if (votingInProgress) {
                    if (!voteResults.containsKey(name)) {
                        voteResults.put(name, "a");
                        updateTab(player);
                        player.sendMessage(ChatColor.GREEN + "Aに投票しました。");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "既に投票済みです。");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "現在投票は行われていません。");
                    return true;
                }
            }
        }

        if (command.getName().equals("b")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String name = player.getName();

                if (votingInProgress) {
                    if (!voteResults.containsKey(name)) {
                        voteResults.put(name, "b");
                        updateTab(player);
                        player.sendMessage(ChatColor.GREEN + "Bに投票しました。");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "既に投票済みです。");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "現在投票は行われていません。");
                    return true;
                }
            }
        }

        if (command.getName().equals("AorBstart")) {
            if (sender instanceof Player) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "/AorBstart 内容A 内容B");
                } else {
                    voteResults.clear();
                    updateTab((Player) sender);
                    if (votingInProgress) {
                        sender.sendMessage(ChatColor.RED + "既に投票が開始されています。");
                        return true;
                    }

                    voteResults.clear();
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(ChatColor.BOLD + "投票が開始されました。");
                    }

                    String a = args[0];
                    String b = args[1];
                    Bukkit.broadcastMessage(ChatColor.BOLD + "A " + a + ChatColor.RESET + " または " + ChatColor.BOLD + "B " + b + ChatColor.RESET + " 選ぶならどっち？");
                    Bukkit.broadcastMessage("/a または /b で投票してください。");

                    votingInProgress = true;
                    displayVotedPlayers();
                }
            }
        }

        if (command.getName().equals("end")) {
            if (sender instanceof Player) {
                if (!votingInProgress) {
                    sender.sendMessage(ChatColor.RED + "現在投票は行われていません。");
                    return true;
                }

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    Scoreboard scoreboard = onlinePlayer.getScoreboard();
                    Objective objective = scoreboard.getObjective(DisplaySlot.PLAYER_LIST);
                    if (objective != null) {
                        for (String playerName : scoreboard.getEntries()) {
                            Score score = objective.getScore(playerName);
                            if (!voteResults.containsKey(playerName)) {
                                score.setScore(0); // 未投票者のスコアを0に設定
                            } else {
                                String vote = voteResults.get(playerName);
                                if (vote.equalsIgnoreCase("a")) {
                                    score.setScore(1); // Aに投票した完了者のスコアを1に設定
                                } else if (vote.equalsIgnoreCase("b")) {
                                    score.setScore(2); // Bに投票した完了者のスコアを2に設定
                                }
                            }
                        }
                    }
                }

                Bukkit.broadcastMessage(ChatColor.BOLD + "投票結果");
                int aVotes = 0;
                int bVotes = 0;

                for (String vote : voteResults.values()) {
                    if (vote.equalsIgnoreCase("a")) {
                        aVotes++;
                    } else if (vote.equalsIgnoreCase("b")) {
                        bVotes++;
                    }
                }

                Bukkit.broadcastMessage(ChatColor.RED + "A: " + aVotes + "票");
                Bukkit.broadcastMessage(ChatColor.BLUE + "B: " + bVotes + "票");


                votingInProgress = false;

            }
        }

        return super.onCommand(sender, command, label, args);
    }

}
