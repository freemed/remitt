package org.remitt.plugin.validation;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.remitt.prototype.ValidationInterface;
import org.remitt.prototype.ValidationMessageType;
import org.remitt.prototype.ValidationResponse;
import org.remitt.prototype.ValidationResponseMessage;
import org.remitt.prototype.ValidationStatus;
import org.remitt.server.Configuration;

public class X12Validator implements ValidationInterface, Serializable {

	private static final long serialVersionUID = 1550990901745054626L;

	protected static final Logger log = Logger.getLogger(X12Validator.class);

	@Override
	public String[] getPluginConfigurationOptions() {
		return null;
	}

	@Override
	public String getPluginName() {
		return "X12Validator";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ValidationResponse validate(String userName, byte[] input)
			throws Exception {
		ValidationResponse response = new ValidationResponse();

		// FIXME: TODO: Determine the type of X12 document by input
		String validatorScript = "4010_837p";

		ScriptEngineManager engineMgr = new ScriptEngineManager();
		ScriptEngine engine = engineMgr.getEngineByName("JavaScript");

		// Fetch and execute script.
		String scriptPath = "/WEB-INF/scripts/org.remitt.plugin.validation.X12Validator/"
				+ validatorScript + ".js";
		String commonScriptPath = "/WEB-INF/scripts/org.remitt.plugin.validation.X12Validator/Common.js";
		String realCommonScriptPath = Configuration.getServletContext()
				.getServletContext().getRealPath(commonScriptPath);
		String realScriptPath = Configuration.getServletContext()
				.getServletContext().getRealPath(scriptPath);

		// Inject objects
		engine.put("log", log);
		engine.put("username", userName);
		engine.put("input", input);

		InputStream is = new FileInputStream(realScriptPath);
		InputStream cis = new FileInputStream(realCommonScriptPath);
		byte[] out = null;
		try {
			// Evaluate common code
			Reader cReader = new InputStreamReader(cis);
			engine.eval(cReader);

			// Evaluate plugin
			Reader reader = new InputStreamReader(is);
			engine.eval(reader);

			Invocable invocableEngine = (Invocable) engine;
			Object output = invocableEngine.invokeFunction("validate");
			if (output != null) {
				if (output instanceof String) {
					out = ((String) output).getBytes();
				} else if (output instanceof List<?>) {
					response
							.setMessages((List<ValidationResponseMessage>) output);
					
					// See what we're returning, form and return it properly.
					ValidationStatus status = ValidationStatus.OK;
					for (ValidationResponseMessage vrm : response.getMessages()) {
						if (vrm.getType() == ValidationMessageType.SERVER_ERROR) {
							status = ValidationStatus.SERVER_ERROR;
						}
						if (vrm.getType() == ValidationMessageType.ERROR
								&& status != ValidationStatus.SERVER_ERROR) {
							status = ValidationStatus.ERROR;
						}
						if (vrm.getType() == ValidationMessageType.WARNING
								&& status != ValidationStatus.SERVER_ERROR
								&& status != ValidationStatus.ERROR) {
							status = ValidationStatus.WARNING;
						}
					}
					response.setStatus(status);
					return response;
				} else {
					out = output.toString().getBytes();
				}
			} else {
				out = new String("").getBytes();
			}
		} catch (ScriptException ex) {
			log.error(ex);
			out = new String("").getBytes();
			log.info("Plugin returned output of " + out);
			response.setStatus(ValidationStatus.SERVER_ERROR);
			response
					.addMessage(new ValidationResponseMessage(
							ValidationMessageType.SERVER_ERROR, "0000",
							new String(out)));
			return response;
		}

		return null;
	}

}
