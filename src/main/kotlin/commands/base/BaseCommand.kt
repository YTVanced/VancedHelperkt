package commands.base

import commandhandler.CommandContext
import commandhandler.CommandManager
import commandhandler.IMessageReactionListener
import database.prefix
import ext.replyWithChecks
import ext.transformToArg
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import type.ArgumentType
import type.CommandType
import java.awt.Color
import javax.annotation.OverridingMethodsMustInvokeSuper

open class BaseCommand(
    open val commandType: CommandType,
    open val commandName: String,
    open val commandDescription: String,
    open val commandArguments: Map<String, ArgumentType> = mapOf(),
    open val commandAliases: List<String> = listOf(commandName),
    open val devOnly: Boolean = false,
    private val addTrashCan: Boolean = true,
) : IMessageReactionListener, KoinComponent {

    val commandManager by inject<CommandManager>()

    val contentIDRegex = "\\b\\d{18}\\b".toRegex()
    val emoteRegex = "<?(a)?:?(\\w{2,32}):(\\d{17,19})>?".toRegex()

    val trashEmote = "\uD83D\uDDD1"

    open val embedBuilder get() = EmbedBuilder().setColor(Color((Math.random() * 0x1000000).toInt()))

    val commandMessage = mutableMapOf<MessageChannel, Message>()
    val userMessage = mutableMapOf<MessageChannel, Message>()

    val MessageChannel.botMessage get() = commandMessage[this]
    val MessageChannel.userMessage get() = this@BaseCommand.userMessage[this]
    var guildId = ""
    private var userMessageId: String = ""

    @OverridingMethodsMustInvokeSuper
    open fun execute(ctx: CommandContext) {
        userMessage[ctx.channel] = ctx.message
        guildId = ctx.guild.id
    }

    @OverridingMethodsMustInvokeSuper
    override fun onReactionAdd(event: MessageReactionAddEvent) {
        if (event.userId != event.channel.userMessage?.author?.id)
            return

        event.user?.let {
            event.reaction.removeReaction(it).queue({
                if (event.reactionEmote.asReactionCode == trashEmote) {
                    event.textChannel.deleteMessagesByIds(listOf(event.channel.botMessage?.id, event.channel.userMessage?.id)).queue(null, ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE){}.handle(ErrorResponse.UNKNOWN_CHANNEL) {}.handle(ErrorResponse.MISSING_ACCESS) {}.handle(ErrorResponse.MISSING_PERMISSIONS) {})
                }
            }, ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE) {}.handle(ErrorResponse.UNKNOWN_CHANNEL) {}.handle(ErrorResponse.MISSING_ACCESS) {})
        }
    }

    override fun onReactionRemove(event: MessageReactionRemoveEvent) {}

    inline fun Message.replyMsg(
        message: String,
        crossinline onSend: (message: Message) -> Unit = {}
    ) {
        replyWithChecks(message) {
            it.addReaction()
            onSend(it)
        }
    }

    inline fun Message.replyMsg(
        embed: MessageEmbed,
        crossinline onSend: (message: Message) -> Unit = {}
    ) {
        replyWithChecks(embed) {
            it.addReaction()
            onSend(it)
        }
    }

    fun Message.addReaction() {
        val messageId = channel.botMessage?.id
        if (messageId != null) {
            channel.removeReactionById(messageId, trashEmote).queue(null, ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE) {})
        }
        commandMessage[channel] = this
        addReaction(trashEmote).queue()
    }

    fun getHelpEmbed(): MessageEmbed = EmbedBuilder().apply {
        setTitle(commandName)
        setDescription("Owner Only: $devOnly\nMinimum required arguments: ${commandArguments.filter { it.value != ArgumentType.Optional }.size}")
        addField("Description", commandDescription, false)
        addField("Usage", "```css\n${guildId.prefix}$commandName ${commandArguments.map { it.transformToArg() }.joinToString(" ")}```", false)
        addField("Aliases", commandAliases.joinToString(" "), false)
    }.build()

}