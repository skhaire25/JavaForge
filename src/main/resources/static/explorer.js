let openTabs = [];

let activeTab = null;

let selectedPackageId = "";
let selectedPackageName = "";

const packageMenu =
	    document.getElementById("packageMenu");
	
let selectedProjectId = "";
let selectedProjectName = "";

let currentInput = "";

let selectedFileId = "";
let selectedFileName = "";

let guestId = localStorage.getItem("guestId");
let socket;

if(!guestId){

    guestId = crypto.randomUUID();

    localStorage.setItem("guestId",guestId);

}

if (!location.search.includes("guestId=")) {

    location.replace(
        "/projects?guestId=" +
        encodeURIComponent(guestId)
    );

}
else {

	const protocol =
	    window.location.protocol === "https:" ? "wss://" : "ws://";

	socket = new WebSocket(
	    protocol +
	    window.location.host +
	    "/console?guestId=" +
	    encodeURIComponent(guestId)
	);

}

const javaFileMenu =
    document.getElementById("javaFileMenu");
	
	let editor = CodeMirror.fromTextArea(
	    document.getElementById("editor"),
		{
		    mode: "text/x-java",
		    theme: "eclipse",
		    lineNumbers: true,
		    indentUnit: 4,
		    tabSize: 4,
		    lineWrapping: false,
		    autoCloseBrackets: true,
		    matchBrackets: true
		}
	);

	editor.on("change", function(){

	    let tab = openTabs.find(t => t.id == activeTab);

	    if(!tab) return;

	    tab.code = editor.getValue();

	    tab.dirty = (tab.code !== tab.savedCode);

	    renderTabs();

	});
	
	const WELCOME_TEXT = window.innerWidth <= 768
	? `Welcome to JavaForge

	Getting Started

	1. Tap the ☰ menu.
	2. Select "New Project".
	3. Expand your project.
	4. Long-press the project to 
	   create a package.
	5. Long-press the package to 
	   create a ☕ Class, 
	   🔷 Interface, 🟣 Enum, 
	   📘 Record or 🏷️ Annotation.
	6. Tap a Java file to open it.
	7. Save and Run your program.

	Happy Coding!`
	: `Welcome to JavaForge

	Getting Started

	1. Click "New Project".
	2. Right-click a project to create a package.
	3. Right-click a package to create a ☕ Class, 🔷 Interface, 🟣 Enum, 📘 Record or 🏷️ Annotation.
	4. Open the Java file.
	5. Save and Run your program.

	Happy Coding!`;

	editor.setValue(WELCOME_TEXT);
	editor.setOption("readOnly", true);


	function clearCompileErrors(){

	    document.querySelectorAll(".javaIcon").forEach(function(icon){

	        icon.textContent = "📄";

	    });

	}

	function markCompileError(fileName){

	    document.querySelectorAll(".javaFile").forEach(function(file){

	        if(file.dataset.name === fileName){

	            const icon =
	                file.parentElement.querySelector(".javaIcon");

	            if(icon){

	                icon.textContent = "❌";

	            }

	        }

	    });

	}
	
socket.onopen = function () {
    console.log("WebSocket Connected");
};

socket.onmessage = function (event) {

    const terminal = document.getElementById("output");

    const text = event.data;
    const lower = text.toLowerCase();

    if (
        lower.includes("error") ||
        lower.includes("exception") ||
        lower.includes("failed")
    ) {

        clearCompileErrors();

        const match = text.match(/File\s*:\s*(.*\.java)/i);

        if (match) {

            markCompileError(match[1].trim());

        }

        terminal.innerHTML +=
            '<span style="color:#ff5252;font-weight:bold;">'
            + text +
            '</span>';

    }
    else if (
        lower.includes("compiled successfully") ||
        lower.includes("compilation successful") ||
        lower.includes("build successful") ||
        lower.includes("success")
    ) {

        clearCompileErrors();

        terminal.innerHTML +=
            '<span style="color:#4caf50;font-weight:bold;">'
            + text +
            '</span>';

    }
    else {

        terminal.innerHTML += text;

    }

    terminal.scrollTop = terminal.scrollHeight;

};

document.getElementById("saveBtn").onclick = function(){

	let tab = openTabs.find(t => t.id == activeTab);

	if(!tab){

	    document.getElementById("output").innerHTML =
	        "Please open a Java file first.";

	    return;

	}

	let fileId = tab.id;

    if(fileId == ""){

        document.getElementById("output").innerHTML =
                "Please open a Java file first.";

        return;

    }

    let sourceCode = editor.getValue();

    fetch("/editor/save",{

        method:"POST",

        headers:{
            "Content-Type":"application/x-www-form-urlencoded"
        },

		body:
		    "guestId=" + encodeURIComponent(guestId)
		    + "&fileId=" + encodeURIComponent(fileId)
		    + "&sourceCode=" + encodeURIComponent(sourceCode)

    })

    .then(response => response.text())

	.then(message => {

	    tab.savedCode = tab.code;

	    tab.dirty = false;

	    renderTabs();

	    document.getElementById("output").innerHTML = message;

	})

    .catch(error => {

        document.getElementById("output").innerHTML =
                "Save Failed";

    });

};

document.getElementById("runBtn").onclick = function () {

    checkUnsaved(function () {

        runProgram(true);

    });

};

function runProgram(saveFirst) {

    let tab = openTabs.find(t => t.id == activeTab);

    if (!tab) {

        document.getElementById("output").innerHTML =
            "Please open a Java file first.";

        return;
    }

    document.getElementById("output").textContent = "";
	if(window.innerWidth <= 768){

	    document.getElementById("consoleSection").style.display = "flex";

	    setTimeout(function(){

	        mobileInput.focus();

	    },200);

	}
	if(window.innerWidth > 768){

	    document.getElementById("output").focus();

	}

    let fileId = tab.id;

    function compileAndRun() {

        fetch("/compiler/run", {

            method: "POST",

            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },

			body:
			    "guestId=" + encodeURIComponent(guestId)
			    + "&fileId=" + encodeURIComponent(fileId)

        });

    }

    if (saveFirst) {

        let sourceCode = editor.getValue();

		fetch("/editor/save", {

		    method: "POST",

		    headers: {
		        "Content-Type":"application/x-www-form-urlencoded"
		    },

			body:
			    "guestId=" + encodeURIComponent(guestId)
			    + "&fileId=" + encodeURIComponent(fileId)
			    + "&sourceCode=" + encodeURIComponent(sourceCode)

		})

		.then(() => {

		    tab.savedCode = tab.code;

		    tab.dirty = false;

		    renderTabs();

		    compileAndRun();

		});

    }
    else {

        editor.setValue(tab.savedCode);

        tab.code = tab.savedCode;

        tab.dirty = false;

        renderTabs();

        compileAndRun();

    }

}

const terminal = document.getElementById("output");
const mobileInput =
    document.getElementById("mobileInput");

const sendInputBtn =
    document.getElementById("sendInputBtn");
	
	function sendMobileInput(){

	    const value = mobileInput.value;

	    socket.send(value);

		if (window.innerWidth <= 768) {

		    document.getElementById("output").innerHTML += value + "<br>";

		}

	    currentInput = "";

	    mobileInput.value = "";

	    mobileInput.focus();

	}
		
		sendInputBtn.onclick = sendMobileInput;
		
		mobileInput.addEventListener("keydown",function(e){

		    if(e.key === "Enter"){

		        e.preventDefault();

		        sendMobileInput();

		    }

		});

terminal.textContent = "Ready...";

if (window.innerWidth > 768) {
    terminal.focus();
}

terminal.onclick = function () {

    if(window.innerWidth <= 768){

        mobileInput.focus();

    }else{

        terminal.focus();

    }

};

terminal.addEventListener("keydown", function(e){
	
	if(window.innerWidth <= 768)
	        return;

    if(e.ctrlKey || e.altKey)
        return;

    e.preventDefault();

    if(e.key === "Enter"){

        socket.send(currentInput);

        terminal.textContent += "\n";

        currentInput = "";

        return;
    }

	if (e.key === "Backspace") {

	    if (currentInput.length > 0) {

	        currentInput = currentInput.slice(0, -1);

	        terminal.textContent =
	            terminal.textContent.slice(0, -1);

	    }

	    return;
	}

    if(e.key.length === 1){

        currentInput += e.key;

        terminal.textContent += e.key;

    }

});

document.getElementById("newProjectBtn").onclick = function(){

    const input = document.getElementById("projectName");

    input.value = "";

    clearValidation("projectName", "projectNameError");

    openDialog("projectDialog");

    input.focus();

};

document.getElementById("cancelProjectBtn").onclick=function(){

    closeDialog("projectDialog");

};

document.getElementById("createProjectBtn").onclick=function(){
	
	if(!validateInput(
	        "projectName",
	        "projectNameError",
	        PROJECT_REGEX,
	        "Only letters, numbers and spaces are allowed."
	    )){
	        return;
	    }

    const projectName =
        document.getElementById("projectName").value;

    fetch("/projects/add",{

        method:"POST",

        headers:{
            "Content-Type":
            "application/x-www-form-urlencoded"
        },

		body:
		"guestId=" + encodeURIComponent(guestId)
		+ "&projectName=" + encodeURIComponent(projectName)

    })
	.then(async response => {

	    if(!response.ok){
	        showError(
	            "projectName",
	            "projectNameError",
	            await response.text()
	        );
	        return;
	    }

	    document.getElementById("projectName").value = "";

	    closeDialog("projectDialog");

	    saveExplorerState();
	    refreshExplorer();
	});

};

let selectedProject = "";

const menu = document.getElementById("projectMenu");

function showProjectMenu(x, y, project){

    selectedProjectId = project.dataset.id;
    selectedProjectName = project.dataset.name;

    menu.style.display = "block";
    menu.style.left = x + "px";
    menu.style.top = y + "px";

}

document.addEventListener("click", function(e){

    if(e.target.closest(".context-menu"))
        return;

    menu.style.display="none";
    packageMenu.style.display="none";
    javaFileMenu.style.display="none";

});

document.getElementById("renameProject").onclick=function(){

    menu.style.display="none";

    openDialog("renameProjectDialog");

    document.getElementById("newProjectName").value =
        selectedProjectName;

    clearValidation("newProjectName", "newProjectNameError");

    document.getElementById("newProjectName").focus();

};

document.getElementById("cancelRenameProjectBtn").onclick=function(){

    closeDialog("renameProjectDialog");

};

document.getElementById("deleteProject").onclick = function(){

    menu.style.display = "none";

    if(confirm("Delete project '" + selectedProjectName + "' ?")){

        fetch("/projects/delete",{

            method:"POST",

            headers:{
                "Content-Type":"application/x-www-form-urlencoded"
            },

			body:
			"guestId=" + encodeURIComponent(guestId)
			+ "&projectId=" + encodeURIComponent(selectedProjectId)

        })
        .then(r => r.text())
        .then(data => {

			saveExplorerState();
			refreshExplorer();

        });

    }

};

document.getElementById("downloadProject").onclick = function () {

    projectMenu.style.display = "none";

	window.location =
	    "/projects/download/" +
	    selectedProjectId +
	    "?guestId=" +
	    encodeURIComponent(guestId);

};

document.getElementById("renameProjectBtn").onclick=function(){
	
	if(!validateInput(
	    "newProjectName",
	    "newProjectNameError",
	    PROJECT_REGEX,
	    "Only letters, numbers and spaces are allowed."
	)){
	    return;
	}

    const newProjectName=
        document.getElementById("newProjectName").value;

    fetch("/projects/rename",{

        method:"POST",

        headers:{
            "Content-Type":
            "application/x-www-form-urlencoded"
        },

		body:
		"guestId=" + encodeURIComponent(guestId)
		+ "&projectId=" + encodeURIComponent(selectedProjectId)
		+ "&newProjectName=" + encodeURIComponent(newProjectName)

    })
	.then(async response => {

	    if(!response.ok){
	        showError(
	            "newProjectName",
	            "newProjectNameError",
	            await response.text()
	        );
	        return;
	    }

	    closeDialog("renameProjectDialog");

		saveExplorerState();
		refreshExplorer();
	});

};

document.getElementById("newPackage").onclick = function(){

    menu.style.display = "none";

    openDialog("packageDialog");

    document.getElementById("packageName").value = "";
	
	clearValidation("packageName", "packageNameError");

    document.getElementById("packageName").focus();

};

document.getElementById("cancelPackageBtn").onclick = function(){

    closeDialog("packageDialog");

};

document.getElementById("createPackageBtn").onclick = function(){
	
	if(!validateInput(
	    "packageName",
	    "packageNameError",
	    PACKAGE_REGEX,
	    "Enter a valid package name (e.g. com.example)."
	)){
	    return;
	}

    const packageName =
        document.getElementById("packageName").value.trim();

    fetch("/packages/add",{

        method:"POST",

        headers:{
            "Content-Type":"application/x-www-form-urlencoded"
        },

		body:
		    "guestId=" + encodeURIComponent(guestId)
		    + "&projectId=" + encodeURIComponent(selectedProjectId)
		    + "&packageName=" + encodeURIComponent(packageName)

    })
	.then(async response => {

	    if(!response.ok){
	        showError(
	            "packageName",
	            "packageNameError",
	            await response.text()
	        );
	        return;
	    }

	    closeDialog("packageDialog");

		saveExplorerState();
		refreshExplorer();
	});

};

function showPackageMenu(x, y, pkg){

    selectedPackageId = pkg.dataset.id;
    selectedPackageName = pkg.dataset.name;

    packageMenu.style.display = "block";
    packageMenu.style.left = x + "px";
    packageMenu.style.top = y + "px";

}

document.getElementById("newJavaClass").onclick = function(){

    packageMenu.style.display = "none";

    openDialog("classDialog");

    document.getElementById("className").value = "";
	
	clearValidation("className", "classNameError");

    document.getElementById("mainMethod").checked = true;

    document.getElementById("className").focus();

};

document.getElementById("cancelClassBtn").onclick = function(){

    closeDialog("classDialog");

};	

document.getElementById("createClassBtn").onclick = function(){
	
	if(!validateInput(
	    "className",
	    "classNameError",
	    JAVA_FILE_REGEX,
	    "Must start with a capital letter and contain only letters and numbers."
	)){
	    return;
	}

    const className =
        document.getElementById("className").value;

    const mainMethod =
        document.getElementById("mainMethod").checked;

    fetch("/javafiles/add",{

        method:"POST",

        headers:{
            "Content-Type":
            "application/x-www-form-urlencoded"
        },

		body:
		"guestId=" + encodeURIComponent(guestId)
		+ "&packageId=" + encodeURIComponent(selectedPackageId)
		+ "&className=" + encodeURIComponent(className)
		+ "&mainMethod=" + mainMethod

    })
	.then(async response => {

	    if(!response.ok){
	        showError(
	            "className",
	            "classNameError",
	            await response.text()
	        );
	        return;
	    }

	    closeDialog("classDialog");

		saveExplorerState();
		refreshExplorer();
	});

};

document.getElementById("newJavaInterface").onclick = function(){

    packageMenu.style.display = "none";

    openDialog("interfaceDialog");

    document.getElementById("interfaceName").value = "";
	
	clearValidation("interfaceName", "interfaceNameError");

    document.getElementById("interfaceName").focus();

};

document.getElementById("newJavaEnum").onclick = function(){

    packageMenu.style.display = "none";

    openDialog("enumDialog");

    document.getElementById("enumName").value = "";

    clearValidation("enumName","enumNameError");

    document.getElementById("enumName").focus();

};

document.getElementById("newJavaRecord").onclick = function(){

    packageMenu.style.display = "none";

    openDialog("recordDialog");

    document.getElementById("recordName").value = "";

    clearValidation("recordName","recordNameError");

    document.getElementById("recordName").focus();

};

document.getElementById("newJavaAnnotation").onclick = function(){

    packageMenu.style.display = "none";

    openDialog("annotationDialog");

    document.getElementById("annotationName").value = "";

    clearValidation("annotationName","annotationNameError");

    document.getElementById("annotationName").focus();

};

document.getElementById("cancelInterfaceBtn").onclick = function(){

    closeDialog("interfaceDialog");

};

document.getElementById("cancelEnumBtn").onclick = function(){

    closeDialog("enumDialog");

};

document.getElementById("cancelRecordBtn").onclick = function(){

    closeDialog("recordDialog");

};

document.getElementById("cancelAnnotationBtn").onclick = function(){

    closeDialog("annotationDialog");

};

document.getElementById("createInterfaceBtn").onclick = function(){
	
	if(!validateInput(
	    "interfaceName",
	    "interfaceNameError",
	    JAVA_FILE_REGEX,
	    "Must start with a capital letter and contain only letters and numbers."
	)){
	    return;
	}

    const interfaceName =
        document.getElementById("interfaceName").value;

    fetch("/javafiles/addInterface",{

        method:"POST",

        headers:{
            "Content-Type":
            "application/x-www-form-urlencoded"
        },

		body:
		"guestId=" + encodeURIComponent(guestId)
		+ "&packageId=" + encodeURIComponent(selectedPackageId)
		+ "&interfaceName=" + encodeURIComponent(interfaceName)

    })
	.then(async response => {

	    if(!response.ok){
	        showError(
	            "interfaceName",
	            "interfaceNameError",
	            await response.text()
	        );
	        return;
	    }

	    closeDialog("interfaceDialog");

		saveExplorerState();
		refreshExplorer();
	});

};

document.getElementById("createEnumBtn").onclick = function(){

    if(!validateInput(
        "enumName",
        "enumNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    )){
        return;
    }

    const enumName =
        document.getElementById("enumName").value;

    fetch("/javafiles/addEnum",{

        method:"POST",

        headers:{
            "Content-Type":"application/x-www-form-urlencoded"
        },

        body:
        "guestId=" + encodeURIComponent(guestId)
        + "&packageId=" + encodeURIComponent(selectedPackageId)
        + "&enumName=" + encodeURIComponent(enumName)

    })
    .then(async response => {

        if(!response.ok){

            showError(
                "enumName",
                "enumNameError",
                await response.text()
            );

            return;
        }

        closeDialog("enumDialog");

        saveExplorerState();

        refreshExplorer();

    });

};

document.getElementById("createRecordBtn").onclick = function(){

    if(!validateInput(
        "recordName",
        "recordNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    )){
        return;
    }

    const recordName =
        document.getElementById("recordName").value;

    fetch("/javafiles/addRecord",{

        method:"POST",

        headers:{
            "Content-Type":"application/x-www-form-urlencoded"
        },

        body:
        "guestId=" + encodeURIComponent(guestId)
        + "&packageId=" + encodeURIComponent(selectedPackageId)
        + "&recordName=" + encodeURIComponent(recordName)

    })
    .then(async response => {

        if(!response.ok){

            showError(
                "recordName",
                "recordNameError",
                await response.text()
            );

            return;
        }

        closeDialog("recordDialog");

        saveExplorerState();

        refreshExplorer();

    });

};

document.getElementById("createAnnotationBtn").onclick = function(){

    if(!validateInput(
        "annotationName",
        "annotationNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    )){
        return;
    }

    const annotationName =
        document.getElementById("annotationName").value;

    fetch("/javafiles/addAnnotation",{

        method:"POST",

        headers:{
            "Content-Type":"application/x-www-form-urlencoded"
        },

        body:
        "guestId=" + encodeURIComponent(guestId)
        + "&packageId=" + encodeURIComponent(selectedPackageId)
        + "&annotationName=" + encodeURIComponent(annotationName)

    })
    .then(async response => {

        if(!response.ok){

            showError(
                "annotationName",
                "annotationNameError",
                await response.text()
            );

            return;
        }

        closeDialog("annotationDialog");

        saveExplorerState();

        refreshExplorer();

    });

};

document.getElementById("renamePackage").onclick=function(){

    packageMenu.style.display="none";

    openDialog("renamePackageDialog");

    document.getElementById("newPackageName").value=selectedPackageName;
	
	clearValidation("newPackageName", "newPackageNameError");

    document.getElementById("newPackageName").focus();

};

document.getElementById("cancelRenamePackageBtn").onclick=function(){

    closeDialog("renamePackageDialog");

};

document.getElementById("renamePackageBtn").onclick=function(){
	
	if(!validateInput(
	    "newPackageName",
	    "newPackageNameError",
	    PACKAGE_REGEX,
	    "Enter a valid package name (e.g. com.example)."
	)){
	    return;
	}

    const newPackageName=
        document.getElementById("newPackageName").value;

    fetch("/packages/rename",{

        method:"POST",

        headers:{
            "Content-Type":
            "application/x-www-form-urlencoded"
        },

		body:
		    "guestId=" + encodeURIComponent(guestId)
		    + "&packageId=" + encodeURIComponent(selectedPackageId)
		    + "&newPackageName=" + encodeURIComponent(newPackageName)

    })
	.then(async response => {

	    if(!response.ok){
	        showError(
	            "newPackageName",
	            "newPackageNameError",
	            await response.text()
	        );
	        return;
	    }

	    closeDialog("renamePackageDialog");

		saveExplorerState();
		refreshExplorer();
	});

};

document.getElementById("deletePackage").onclick=function(){

    packageMenu.style.display="none";

    if(confirm("Delete package '"+selectedPackageName+"' ?")){

        fetch("/packages/delete",{

            method:"POST",

            headers:{
                "Content-Type":
                "application/x-www-form-urlencoded"
            },

			body:
			    "guestId=" + encodeURIComponent(guestId)
			    + "&packageId=" + encodeURIComponent(selectedPackageId)

        })
        .then(r=>r.text())
        .then(data=>{

			saveExplorerState();
			refreshExplorer();

        });

    }

};

function showJavaFileMenu(x, y, file){

    selectedFileId = file.dataset.id;
    selectedFileName = file.dataset.name;

    javaFileMenu.style.display = "block";
    javaFileMenu.style.left = x + "px";
    javaFileMenu.style.top = y + "px";

}

document.getElementById("renameJavaFile").onclick=function(){

    javaFileMenu.style.display="none";

    openDialog("renameJavaDialog");

    document.getElementById("newJavaFileName").value=
        selectedFileName.replace(".java","");
		
	clearValidation("newJavaFileName", "newJavaFileNameError");

};

document.getElementById("renameJavaBtn").onclick=function(){
	
	if(!validateInput(
	    "newJavaFileName",
	    "newJavaFileNameError",
	    JAVA_FILE_REGEX,
	    "Must start with a capital letter and contain only letters and numbers."
	)){
	    return;
	}

    const newName=
        document.getElementById("newJavaFileName").value;

    fetch("/javafiles/rename",{

        method:"POST",

        headers:{
            "Content-Type":
            "application/x-www-form-urlencoded"
        },

		body:
		"guestId=" + encodeURIComponent(guestId)
		+ "&fileId=" + encodeURIComponent(selectedFileId)
		+ "&newFileName=" + encodeURIComponent(newName)

    })
	.then(async response => {

	    if(!response.ok){
	        showError(
	            "newJavaFileName",
	            "newJavaFileNameError",
	            await response.text()
	        );
	        return;
	    }

	    closeDialog("renameJavaDialog");

		saveExplorerState();
		refreshExplorer();
	});

};

document.getElementById("deleteJavaFile").onclick = function(){

    javaFileMenu.style.display = "none";

    if(confirm("Delete Java file '" + selectedFileName + "' ?")){

        fetch("/javafiles/delete",{

            method:"POST",

            headers:{
                "Content-Type":"application/x-www-form-urlencoded"
            },

			body:
			    "guestId=" + encodeURIComponent(guestId)
			    + "&fileId=" + encodeURIComponent(selectedFileId)

        })
        .then(r => r.text())
        .then(data => {

			saveExplorerState();
			refreshExplorer();

        });

    }

};

document.getElementById("cancelRenameJavaBtn").onclick = function(){

    closeDialog("renameJavaDialog");

};

const themeBtn = document.getElementById("themeBtn");

function applyTheme(theme){

    if(theme === "dark"){

        document.body.classList.add("dark");

        themeBtn.innerHTML = "☀️";

        editor.setOption("theme","dracula");

    }
    else{

        document.body.classList.remove("dark");

        themeBtn.innerHTML = "🌙";

        editor.setOption("theme","eclipse");

    }

}

applyTheme(localStorage.getItem("theme") || "light");

themeBtn.onclick = function(){

    const theme =
        document.body.classList.contains("dark")
        ? "light"
        : "dark";

    localStorage.setItem("theme", theme);

    applyTheme(theme);

};

function initSplit() {

    if (window.innerWidth <= 768) return;

    Split([".left", ".right"], {

        sizes: [22, 78],

        minSize: [220, 400],

        gutterSize: 6,

        gutter: () => document.getElementById("verticalDivider")

    });

}

function initConsoleSplit() {

    if (window.innerWidth <= 768) return;

    Split([".editorPanel", ".console"], {

        direction: "vertical",

        sizes: [72, 28],

        minSize: [200, 120],

        gutterSize: 6,

        gutter: () => document.getElementById("horizontalDivider")

    });

}

initSplit();
initConsoleSplit();

function openDialog(id){

    document.getElementById("dialogOverlay").style.display="block";

    document.getElementById(id).style.display="block";

}

function closeDialog(id){

    document.getElementById("dialogOverlay").style.display="none";

    document.getElementById(id).style.display="none";

}

function saveExplorerState(){

    const opened = [];

    document.querySelectorAll("details").forEach(function(item){

        if(item.open){

            opened.push(item.dataset.key);

        }

    });

    localStorage.setItem(
        "explorerState",
        JSON.stringify(opened)
    );

}

function restoreExplorerState(){

    const opened =
        JSON.parse(
            localStorage.getItem("explorerState")
        ) || [];

    document.querySelectorAll("details").forEach(function(item){

        if(opened.includes(item.dataset.key)){

            item.open = true;

        }

    });

}

const PROJECT_REGEX = /^[A-Za-z0-9 ]+$/;
const PACKAGE_REGEX = /^(?=.*[A-Za-z])[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)*$/;
const JAVA_FILE_REGEX = /^[A-Z][A-Za-z0-9]*$/;

function validateInput(inputId, errorId, regex, message){

    const input = document.getElementById(inputId);
    const error = document.getElementById(errorId);

    const value = input.value.trim();

    if(value === ""){

        error.textContent = "This field is required.";
        input.classList.add("invalid");
        return false;

    }

    if(!regex.test(value)){

        error.textContent = message;
        input.classList.add("invalid");
        return false;

    }

    error.textContent = "";
    input.classList.remove("invalid");
    return true;

}

function clearValidation(inputId, errorId){

    document.getElementById(inputId).classList.remove("invalid");
    document.getElementById(errorId).textContent = "";

}

function showError(inputId, errorId, message){

    document.getElementById(inputId)
            .classList.add("invalid");

    document.getElementById(errorId)
            .textContent = message;
}

function getJavaIcon(sourceCode){

    if(sourceCode.includes("public interface"))
        return "🔷";

    if(sourceCode.includes("public enum"))
        return "🟣";

    if(sourceCode.includes("public record"))
        return "📘";

    if(sourceCode.includes("@interface"))
        return "🏷️";

    return "☕";
}

function openTab(id,name,code){

    let existing = openTabs.find(t => t.id == id);

    if(existing){

        activeTab = id;

        switchTab(id);

        return;

    }

	openTabs.push({

	    id: id,

	    name: name,

	    code: code,

	    savedCode: code,

	    icon: getJavaIcon(code),

	    dirty: false

	});

	    activeTab = id;

	    renderTabs();

	    switchTab(id);

	}

function switchTab(id){

    let tab = openTabs.find(t => t.id == id);

    if(!tab) return;

    activeTab = id;

    document.getElementById("fileId").value = tab.id;

	editor.setOption("readOnly", false);
	editor.setValue(tab.code);

    renderTabs();

}

function closeTab(id){

    let index = openTabs.findIndex(t => t.id == id);

    if(index == -1) return;

    openTabs.splice(index,1);

	if(openTabs.length == 0){

	    activeTab = null;

	    editor.setValue(WELCOME_TEXT);
	    editor.setOption("readOnly", true);

	    renderTabs();

	    return;

	}

    switchTab(openTabs[openTabs.length-1].id);

}

function renderTabs(){

    const bar = document.getElementById("editorTabs");

    bar.innerHTML = "";

    openTabs.forEach(tab=>{

        const div = document.createElement("div");

		div.className =
		    "editorTab" +
		    (tab.id==activeTab ? " active" : "");

		div.dataset.name = tab.name;

		div.innerHTML = `
		    <span>${tab.icon}</span>
		    <span>${tab.name}${tab.dirty ? " *" : ""}</span>
		    <span class="closeTab">&times;</span>
		`;

        div.onclick = () => switchTab(tab.id);

		div.querySelector(".closeTab").onclick = function(e){

		    e.stopPropagation();

		    checkUnsaved(function(){

		        closeTab(tab.id);

		    });

		};

        bar.appendChild(div);

    });

}

function checkUnsaved(nextAction){

    let tab = openTabs.find(t => t.id == activeTab);

    if(!tab || !tab.dirty){

        nextAction();
        return;

    }

    document.getElementById("unsavedMessage").textContent =
        "Do you want to save changes to " + tab.name + "?";

    openDialog("unsavedDialog");

    document.getElementById("saveUnsavedBtn").onclick = function(){

        document.getElementById("saveBtn").click();

        closeDialog("unsavedDialog");

        nextAction();

    };

	document.getElementById("dontSaveBtn").onclick = function () {

	    let tab = openTabs.find(t => t.id == activeTab);

	    editor.setValue(tab.savedCode);

	    tab.code = tab.savedCode;

	    tab.dirty = false;

	    renderTabs();

	    closeDialog("unsavedDialog");

	    runProgram();

	};

    document.getElementById("cancelUnsavedBtn").onclick = function(){

        closeDialog("unsavedDialog");

    };

}

document.getElementById("projectName").addEventListener("input", function(){

    validateInput(
        "projectName",
        "projectNameError",
        PROJECT_REGEX,
        "Only letters, numbers and spaces are allowed."
    );

});

document.getElementById("newProjectName").addEventListener("input", function(){

    validateInput(
        "newProjectName",
        "newProjectNameError",
        PROJECT_REGEX,
        "Only letters, numbers and spaces are allowed."
    );

});

document.getElementById("packageName").addEventListener("input", function(){

    validateInput(
        "packageName",
        "packageNameError",
        PACKAGE_REGEX,
        "Enter a valid package name (e.g. com.example)."
    );

});

document.getElementById("newPackageName").addEventListener("input", function(){

    validateInput(
        "newPackageName",
        "newPackageNameError",
        PACKAGE_REGEX,
        "Enter a valid package name (e.g. com.example)."
    );

});

document.getElementById("className").addEventListener("input", function(){

    validateInput(
        "className",
        "classNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    );

});

document.getElementById("interfaceName").addEventListener("input", function(){

    validateInput(
        "interfaceName",
        "interfaceNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    );

});

document.getElementById("enumName").addEventListener("input",function(){

    validateInput(
        "enumName",
        "enumNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    );

});

document.getElementById("recordName").addEventListener("input",function(){

    validateInput(
        "recordName",
        "recordNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    );

});

document.getElementById("annotationName").addEventListener("input",function(){

    validateInput(
        "annotationName",
        "annotationNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    );

});

document.getElementById("newJavaFileName").addEventListener("input", function(){

    validateInput(
        "newJavaFileName",
        "newJavaFileNameError",
        JAVA_FILE_REGEX,
        "Must start with a capital letter and contain only letters and numbers."
    );

});

function refreshExplorer() {

    const sidebar = document.getElementById("sidebar");
    const wasOpen = sidebar.classList.contains("open");

	fetch("/projects/explorerSection?guestId=" +
	      encodeURIComponent(guestId))
        .then(r => r.text())
        .then(html => {

            sidebar.innerHTML =
                new DOMParser()
                    .parseFromString(html, "text/html")
                    .getElementById("sidebar").innerHTML;

            restoreExplorerState();
            attachExplorerEvents();
            initSplit();

            if (wasOpen && window.innerWidth <= 768) {

                setTimeout(function () {

                    sidebar.classList.add("open");

                }, 0);

            }

        });

}

function attachExplorerEvents(){
	
	const newProjectBtn = document.getElementById("newProjectBtn");

	if (newProjectBtn) {

	    newProjectBtn.onclick = function () {

	        openDialog("projectDialog");

	    };

	}

	document.querySelectorAll(".project").forEach(function(project){

	    project.addEventListener("contextmenu", function(e){

	        e.preventDefault();

	        showProjectMenu(e.pageX, e.pageY, this);

	    });

	    let pressTimer;

	    project.addEventListener("touchstart", function(e){

	        pressTimer = setTimeout(() => {

	            e.preventDefault();

	            showProjectMenu(
	                e.touches[0].pageX,
	                e.touches[0].pageY,
	                project
	            );

	        }, 600);

	    });

	    project.addEventListener("touchend", function(){

	        clearTimeout(pressTimer);

	    });

	    project.addEventListener("touchmove", function(){

	        clearTimeout(pressTimer);

	    });

	    project.addEventListener("touchcancel", function(){

	        clearTimeout(pressTimer);

	    });

	});
	
	document.querySelectorAll(".package").forEach(function(pkg){

	    pkg.addEventListener("contextmenu", function(e){

	        e.preventDefault();

	        showPackageMenu(e.pageX, e.pageY, this);

	    });

	    let pressTimer;

	    pkg.addEventListener("touchstart", function(e){

	        pressTimer = setTimeout(() => {

	            e.preventDefault();

	            showPackageMenu(
	                e.touches[0].pageX,
	                e.touches[0].pageY,
	                pkg
	            );

	        }, 600);

	    });

	    pkg.addEventListener("touchend", function(){

	        clearTimeout(pressTimer);

	    });

	    pkg.addEventListener("touchmove", function(){

	        clearTimeout(pressTimer);

	    });

	    pkg.addEventListener("touchcancel", function(){

	        clearTimeout(pressTimer);

	    });

	});
	
	let longPress = false;
	
	document.querySelectorAll(".javaFile").forEach(function(file){

	    file.onclick = function(){
			
			if (longPress) {
			            longPress = false;
			            return;
			        }

					fetch(
					    "/editor/" +
					    this.dataset.id +
					    "?guestId=" +
					    encodeURIComponent(guestId)
					)
	        .then(response => response.json())
	        .then(data => {

	            openTab(
	                data.fileId,
	                data.fileName,
	                data.sourceCode
	            );

	            document.getElementById("fileId").value = data.fileId;

	            document.getElementById("output").innerHTML =
	                "Opened : " + data.fileName;
					
					if (window.innerWidth <= 768) {

					    setTimeout(function () {

					        const sidebar = document.getElementById("sidebar");

					        if (sidebar) {
					            sidebar.classList.remove("open");
					        }

					    }, 100);

					}

	        });

	    };

	    file.addEventListener("contextmenu", function(e){

	        e.preventDefault();

	        showJavaFileMenu(e.pageX, e.pageY, this);

	    });

	    let pressTimer;

	    file.addEventListener("touchstart", function(e){

	        pressTimer = setTimeout(() => {
				
				longPress = true;

	            e.preventDefault();

	            showJavaFileMenu(
	                e.touches[0].pageX,
	                e.touches[0].pageY,
	                file
	            );

	        }, 600);

	    });

	    file.addEventListener("touchend", function(){

	        clearTimeout(pressTimer);

	    });

	    file.addEventListener("touchmove", function(){

	        clearTimeout(pressTimer);

	    });

	    file.addEventListener("touchcancel", function(){

	        clearTimeout(pressTimer);

	    });

	});

}

restoreExplorerState();
attachExplorerEvents();

document.getElementById("menuBtn").onclick = function () {

    const sidebar = document.getElementById("sidebar");

    sidebar.classList.toggle("open");

};

document.getElementById("showEditor").onclick = function () {

    const sidebar = document.getElementById("sidebar");

    sidebar.classList.remove("open");
    document.getElementById("editorSection").style.display = "flex";
    document.getElementById("consoleSection").style.display = "none";
};

document.getElementById("showConsole").onclick = function () {

    const sidebar = document.getElementById("sidebar");

    sidebar.classList.remove("open");
    document.getElementById("editorSection").style.display = "none";
    document.getElementById("consoleSection").style.display = "flex";
};

document.addEventListener("click", function (e) {
	const sidebar = document.getElementById("sidebar");
    if (window.innerWidth > 768) return;

    if (
        !sidebar.contains(e.target) &&
        e.target.id !== "menuBtn" &&
        !e.target.closest("#menuBtn")
    ) {
        sidebar.classList.remove("open");
    }
});

if (window.innerWidth <= 768) {
    document.getElementById("consoleSection").style.display = "none";
}