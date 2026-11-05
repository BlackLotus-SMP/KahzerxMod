package com.kahzerx.kahzerxmod.extensions.joinMOTDExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class JoinMOTDExtension extends GenericExtension implements Extensions {
    public PermsExtension permsExtension;
    private ExtensionManager em;
    public JoinMOTDExtension(HashMap<String, String> fileSettings) {
        super(new JoinMOTDSettings(fileSettings, "joinMOTD", "Sends a custon message on player join."));
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.permsExtension = (PermsExtension) em.getExtensions().get("perms");
        this.em = em;
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new JoinMOTDCommand().register(dispatcher, this);
    }

    public void updateMessage(CommandSourceStack source, String message) {
        this.extensionSettings().setMessage(message);
        source.sendSuccess(MarkEnum.TICK.appendMessage("New join message configured!"), false);
        source.sendSuccess(() -> getFormatted(message), false);
    }

    @Override
    public void onPlayerConnected(ServerPlayer player) {
        if (this.extensionSettings().isEnabled() && !this.extensionSettings().getMessage().equals("")) {
            player.sendSystemMessage(getFormatted(this.extensionSettings().getMessage()));
        }
    }

    public MutableComponent getFormatted(String message) {
        // /joinMOTD set awdawd [%1awdawd1]{click=/say a} \n[awdawd test]{display=test} a [%dawdawd1]{display=test2,click=/say a} a
        message = message.replace('%', '§');
        message = message.replace("\\n", "\n");
        List<StyleData> styled = new ArrayList<>();
        List<StyleData> styles = new ArrayList<>();
        int posAtStyledOpen = -1;
        int posAtStyleOpen = -1;

        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) == '[') {
                posAtStyledOpen = i;
            }
            if (message.charAt(i) == ']' && posAtStyledOpen != -1) {
                styled.add(new StyleData(posAtStyledOpen, i));
                posAtStyledOpen = -1;
            }

            if (message.charAt(i) == '{') {
                posAtStyleOpen = i;
            }
            if (message.charAt(i) == '}' && posAtStyleOpen != -1) {
                styles.add(new StyleData(posAtStyleOpen, i));
                posAtStyleOpen = -1;
            }
        }

        List<StyleData> correctStyled = new ArrayList<>();
        List<StyleData> correctStyles = new ArrayList<>();
        for (StyleData style_t : styled) {
            for (StyleData style_t2 : styles) {
                if (style_t.ends() + 1 == style_t2.starts()) {
                    correctStyled.add(style_t);
                    correctStyles.add(style_t2);
                    break;
                }
            }
        }

        MutableComponent text = Component.literal("");
        int actualStartPos = 0;
        if (correctStyled.size() == 0) {
            text.append(message);
        }
        for (int i = 0; i < correctStyled.size(); i++) {
            if (correctStyled.get(i).starts() == actualStartPos) {
                String custom = message.substring(correctStyled.get(i).starts() + 1, correctStyled.get(i).ends());
                String styleFormat = message.substring(correctStyles.get(i).starts(), correctStyles.get(i).ends() + 1);
                text.append(format(custom, styleFormat));
            } else {
                text.append(Component.literal(message.substring(actualStartPos, correctStyled.get(i).starts())));
                String custom = message.substring(correctStyled.get(i).starts() + 1, correctStyled.get(i).ends());
                String styleFormat = message.substring(correctStyles.get(i).starts(), correctStyles.get(i).ends() + 1);
                text.append(format(custom, styleFormat));
            }
            actualStartPos = correctStyles.get(i).ends() + 1;
        }
        if (actualStartPos != 0) {
            text.append(message.substring(actualStartPos));
        }
        return text;
    }

    public MutableComponent format(String text, String formats) {
        MutableComponent msg = Component.literal(text);
        String[] configs = formats.substring(1, formats.length() - 1).split(",");
        for (String config : configs) {
            config = config.strip();
            if (config.contains("=")) {
                String[] kv = config.split("=");
                if (kv.length != 2) {
                    continue;
                }
                if (kv[0].strip().equals("display")) {
                    String hoverText = kv[1].strip();
                    msg.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverText))));
                }
                if (kv[0].strip().equals("click")) {
                    String click = kv[1].strip();
                    msg.withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand(click)));
                }
            }
        }
        return msg;
    }

    @Override
    public JoinMOTDSettings extensionSettings() {
        return (JoinMOTDSettings) this.getSettings();
    }

    public PermsExtension getPermsExtension() {
        return permsExtension;
    }

    private record StyleData(int starts, int ends) { }

    public ExtensionManager getEm() {
        return em;
    }
}
