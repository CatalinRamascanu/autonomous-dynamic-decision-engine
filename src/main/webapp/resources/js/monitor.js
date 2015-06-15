 $(function() {
	$( "#rule-accordion" ).accordion({
		active: false,
		collapsible: true,
		heightStyle: "content"
	});

	$( "#action-accordion" ).accordion({
		active: false,
		collapsible: true,
		heightStyle: "content"
	});

	$( "#input-accordion" ).accordion({
		active: false,
		collapsible: true,
		heightStyle: "content"
	});

	$( "#fetcher-accordion" ).accordion({
		active: false,
		collapsible: true,
		heightStyle: "content"
	});

	$( "#tabs" ).tabs();

	initializeLoggingStatus();
	initializeEngineStatus();
	initializeTerminal();
	initializeInsertDataTab();
	startRequestingUpdatesFromServer();
});

function startRequestingUpdatesFromServer() {
	var intervalID = setInterval(function(){
		if (loggingState && engineState){
			$.get("/monitor_updates", function(data, status){
				if (status == "success"){
					oldtop = $('#term_demo').scrollTop();
					var log = "";
					for (i = 0; i < data.length; i++) { 
					    log += "ADDE> " + data[i] + "\n";
					}
					if (log != ""){
						terminal.echo(log);
						$('#term_demo').scrollTop(oldtop);
					}		
			}
			}).fail(function() {
				alert("Communication with the engine has unexpectedly ended. Closing page.");
				document.write('<script type="text/undefined">');
				clearInterval(intervalID);
				});

		$.get("/isRunning", function(data, status){
			if (status == "success"){
					engineState = data;
					if (engineState == true){
						$("#engineStateButton").button('option', 'label', "Stop engine");
					}
				else{
					engineState = false;
				}
			}
			});
		}
    }, 200);
}

function initializeInsertDataTab() {
	for (index in inputDataList){
    	$('#inputSelectList').append($('<option/>', { 
	        text : inputDataList[index]["input-id"],
	    }));
    } 
	$( "#inputSelectList" ).selectmenu({
		change: function( event, ui ) {
			console.log(ui.item.index);
			console.log(inputDataList[ui.item.index]);
			generateForm(inputDataList[ui.item.index]);
		}
	});

    generateForm(inputDataList[0]);

    // Hook up the sendDataButton to log to the console
	document.getElementById('sendDataButton').addEventListener('click',function() {
		// Get the value from the editor
		dataValue = editor.getValue();
		console.log(dataValue);
		console.log(jQuery.param(dataValue));

		//Creating url for inserting data
		url = "/insert_input?inputId=";
		url += editor.schema.title + "&";
		url += jQuery.param(dataValue);
		console.log("Insertion URL is : " + url);

		// Sending data
		$.get(url, function(data, status){
		    console.log("Status = " + status + " : data = " + data);
		});	

	});
	$("#sendDataButton").button()
}

var loggingState = true;
function initializeLoggingStatus(){
	$( "#loggingStateButton" ).html("Stop logging");
	$( "#loggingStateButton" )
	.button()
	.click(function( event ) {
		if (loggingState == true){
			$(this).button('option', 'label', "Start logging");
			loggingState = false;
		}
		else{
			$(this).button('option', 'label', "Stop logging");
			loggingState = true;
		}
	});
}

var engineState = false;
function initializeEngineStatus() {
	setInterval(function(){
		if (engineState){
			$("#engineStatusText").html("Running");
			$("#engineStatusText").css( "color","green" );
		}
		else{
			$("#engineStatusText").html("Not running");
			$("#engineStatusText").css( "color","red" );
		}
	}, 100);

	$("#engineStateButton").html("Start engine");
	$.get("/isRunning", function(data, status){
		console.log("isRunning: " + status + " = " + data);
		if (status == "success"){
				engineState = data;
				if (engineState == true){
					$("#engineStateButton").button('option', 'label', "Stop engine");
				}
			else{
				engineState = false;
			}
		}
		else{
			document.write('<script type="text/undefined">');
		}
	});

	$( "#engineStateButton" )
	.button()
	.click(function( event ) {
		if (engineState == true){
			$(this).prop('disabled', true);
			$.get("/shutdown", function(data, status){
				console.log(status + " = " + data);
				if (status == "success"){
					console.log(data);
					$("#engineStateButton").button('option', 'label', "Start engine");
					engineState = false;
				}
				$("#engineStateButton").prop('disabled', false);
		    });	
			
		}
		else{
			$(this).prop('disabled', true);
			$.get("/startup", function(data, status){
				console.log(status + " = " + data);
				if (status == "success"){
					console.log(data);
					if (data == true){
						$("#engineStateButton").button('option', 'label', "Stop engine");
						engineState = true;
					}
					else{
						engineState = false;
					}
				}
				$("#engineStateButton").prop('disabled', false);
			});	
		}
	});
}

var terminal = null;
function initializeTerminal() {
	terminal = $('#term_demo').terminal(function(command, term) {
		if (command !== '') {
			try {
				var result = window.eval(command);
				if (result !== undefined) {
					term.echo(new String(result));
				}
			} catch(e) {
			  term.error(new String(e));
			}
			} else {
				term.echo('');
			}
		}, 
		{
			greetings: 'ADDE - Status Monitor',
			name: 'adde-terminal',
			height: '100%',
			width: '100%',
			prompt: 'ADDE> ',
			  // keydown: function(event, terminal) { return false;},
			  // keypress: function(event, terminal) { return false;}
		}
	);
}

var editor = null;
function generateForm(inputJson) {
	formSchema = createInputFormSchema(inputJson);

	JSONEditor.defaults.options.theme = 'jqueryui';
	// Initialize the editor with a JSON schema
	if (editor != null){
		editor.destroy();
		editor = null;
	}
	editor = new JSONEditor(document.getElementById('editor_holder'),{
		schema: formSchema,
		disable_edit_json : true,
		disable_properties : true,
	});
}

function createInputFormSchema(inputJson) {
	var formSchema = {};
	formSchema.type = "object";
	formSchema.title = inputJson["input-id"];
	formSchema.properties = {};
	for (index in inputJson.data){
		dataObj = inputJson.data[index];
		var type = null;
		if (dataObj.type == "int" || dataObj.type == "long" ){
			type = "integer";
		}
		else{
			if (dataObj.type == "string"){
				type = "string";
			}
			else{
				type = "number";
			}
		}
		// dataName = dataObj.name + " [" + dataObj.type + "]";
		dataName = dataObj.name;
		formSchema.properties[dataName] = {};
		formSchema.properties[dataName].type = type;
	}
	return formSchema;
}
