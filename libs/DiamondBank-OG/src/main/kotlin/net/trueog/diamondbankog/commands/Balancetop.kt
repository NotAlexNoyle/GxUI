package net.trueog.diamondbankog.commands

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.trueog.diamondbankog.Config
import net.trueog.diamondbankog.DiamondBankOG
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import kotlin.math.ceil

class Balancetop : CommandExecutor {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        GlobalScope.launch {
            if (DiamondBankOG.economyDisabled) {
                sender.sendMessage(DiamondBankOG.mm.deserialize("${Config.prefix}<reset>: <red>The economy is disabled because of a severe error. Please notify a staff member."))
                return@launch
            }

            if (!sender.hasPermission("diamondbank-og.balancetop")) {
                sender.sendMessage(DiamondBankOG.mm.deserialize("${Config.prefix}<reset>: <red>You do not have permission to use this command."))
                return@launch
            }

            if (args == null) return@launch
            if (args.size > 1) {
                sender.sendMessage(DiamondBankOG.mm.deserialize("${Config.prefix}<reset>: <red>Please (only) optionally provide the page number."))
                return@launch
            }

            var offset = 0
            var index = 1
            if (args.size == 1) {
                try {
                    index = args[0].toInt()
                } catch (_: Exception) {
                    sender.sendMessage("Invalid argument")
                    return@launch
                }
                offset = 10 * (index - 1)
            }

            val baltop = DiamondBankOG.postgreSQL.getBaltop(offset)
            if (baltop == null) {
                sender.sendMessage("${Config.prefix}<reset>: <red>Something went wrong while trying to get the information for balancetop.")
                return@launch
            }
            val numberOfRows = DiamondBankOG.postgreSQL.getNumberOfRows()
            if (numberOfRows == null) {
                sender.sendMessage("${Config.prefix}<reset>: <red>Something went wrong while trying to get the information for balancetop.")
                return@launch
            }

            if (index > ceil(numberOfRows / 10.0)) {
                sender.sendMessage("The amount of pages only goes up to $numberOfRows")
                return@launch
            }
            var baltopMessage =
                ("<yellow>---- <gold>Balancetop <yellow>-- <gold>Page <red>$index<gold>/<red>${ceil(numberOfRows / 10.0).toLong()} <yellow>----<reset>")
            baltop.forEach {
                if (it.key == null) {
                    sender.sendMessage("${Config.prefix}<reset>: <red>Something went wrong while trying to get the information for balancetop.")
                    return@launch
                }
                baltopMessage += "\n<red>${baltopMessage.lines().size + (10 * (index - 1))}<reset>. ${if (it.key == sender.name) "<red>" else ""}${it.key}<reset>, ${it.value}"
            }

            sender.sendMessage(DiamondBankOG.mm.deserialize(baltopMessage))
        }
        return true
    }
}