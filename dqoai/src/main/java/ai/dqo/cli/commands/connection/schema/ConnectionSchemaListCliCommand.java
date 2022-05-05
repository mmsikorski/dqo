/*
 * Copyright © 2021 DQO.ai (support@dqo.ai)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.dqo.cli.commands.connection.schema;

import ai.dqo.cli.commands.BaseCommand;
import ai.dqo.cli.commands.ICommand;
import ai.dqo.cli.commands.connection.impl.ConnectionService;
import ai.dqo.cli.commands.connection.impl.models.ConnectionListModel;
import ai.dqo.cli.terminal.FormattedTableDto;
import ai.dqo.cli.terminal.TerminalReader;
import ai.dqo.cli.terminal.TerminalWriter;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * "connection schema list" 3nd level cli command.
 */
@Component
@Scope("prototype")
@CommandLine.Command(name = "list", description = "List schemas in source connection")
public class ConnectionSchemaListCliCommand extends BaseCommand implements ICommand {
	private final ConnectionService connectionService;
	private final TerminalReader terminalReader;
	private final TerminalWriter terminalWriter;

	@Autowired
	public ConnectionSchemaListCliCommand(ConnectionService connectionService,
								  	TerminalReader terminalReader,
									TerminalWriter terminalWriter) {
		this.connectionService = connectionService;
		this.terminalReader = terminalReader;
		this.terminalWriter = terminalWriter;
	}

	@CommandLine.Option(names = {"-n", "--name"}, description = "Connection name filter", required = false)
	private String name;

	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 * @throws Exception if unable to compute a result
	 */
	@Override
	public Integer call() throws Exception {
		if (Strings.isNullOrEmpty(this.name)) {
			throwRequiredParameterMissingIfHeadless("--name");
			this.name = this.terminalReader.prompt("Connection name (--name)", null, false);
		}

		FormattedTableDto<ConnectionListModel> formattedTable = this.connectionService.loadConnectionTable(name);
		this.terminalWriter.writeTable(formattedTable, true);

		return 0;
	}
}
