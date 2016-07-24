;This file will be executed next to the application bundle image
;I.e. current directory will contain folder with application files

#define MyAppName "Les Tables De Daniel"
#define MyAppShortName "LesTablesDeDaniel"
#define MyAppVersion "1.0"
#define MyAppPublisher "Gwendal ROULLEAU"
#define MyAppURL "https://github.com/dalgwen/LesTablesDeDaniel"
#define MyAppExeName "LesTablesDeDaniel-1.0.exe"

[Setup]
AppId={{net.roulleau.tables}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppCopyright=Copyright (C) 2016
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
;DisableStartupPrompt=Yes
;DisableProgramGroupPage=Yes
;DisableReadyPage=Yes
;DisableFinishedPage=Yes
;DisableWelcomePage=Yes
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
;Optional License
LicenseFile=gplv3.txt
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename={#MyAppShortName}-setup
Compression=lzma
SolidCompression=yes
;PrivilegesRequired=lowest
SetupIconFile={#MyAppShortName}-{#MyAppVersion}\{#MyAppShortName}-{#MyAppVersion}.ico
UninstallDisplayIcon={app}\{#MyAppShortName}-{#MyAppVersion}.ico
UninstallDisplayName={#MyAppShortName}
WizardImageStretch=No
WizardSmallImageFile={#MyAppShortName}-{#MyAppVersion}-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "french"; MessagesFile: "compiler:Languages\French.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "{#MyAppShortName}-{#MyAppVersion}\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppShortName}-{#MyAppVersion}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppShortName}-{#MyAppVersion}.ico"; Check: returnTrue()
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}";  IconFilename: "{app}\{#MyAppShortName}-{#MyAppVersion}.ico"; Tasks: desktopicon; Check: returnFalse()
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

[Run]
Filename: "{app}\{#MyAppExeName}"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#MyAppShortName}}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\{#MyAppExeName}"; Parameters: "-install -svcName ""{#MyAppShortName}"" -svcDesc ""{#MyAppShortName}"" -mainExe ""{#MyAppExeName}""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\{#MyAppExeName} "; Parameters: "-uninstall -svcName {#MyAppShortName} -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
