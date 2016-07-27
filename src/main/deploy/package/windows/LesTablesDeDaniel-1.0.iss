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
LicenseFile=gplv3-en.txt
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename={#MyAppShortName}-setup
Compression=lzma
SolidCompression=yes
SetupIconFile={#MyAppShortName}-{#MyAppVersion}\{#MyAppShortName}-{#MyAppVersion}.ico
UninstallDisplayIcon={app}\{#MyAppShortName}-{#MyAppVersion}.ico
UninstallDisplayName={#MyAppShortName}
WizardImageStretch=No
WizardSmallImageFile={#MyAppShortName}-{#MyAppVersion}-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=
ChangesAssociations=yes

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
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppShortName}-{#MyAppVersion}.ico"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}";  IconFilename: "{app}\{#MyAppShortName}-{#MyAppVersion}.ico"; Tasks: desktopicon
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#MyAppShortName}}"; Flags: nowait postinstall skipifsilent;

[Registry]
Root: HKCR; Subkey: ".tdd"; ValueType: string; ValueName: ""; ValueData: "TableDeDaniel"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "TableDeDaniel"; ValueType: string; ValueName: ""; ValueData: "Table de Daniel"; Flags: uninsdeletekey
Root: HKCR; Subkey: "TableDeDaniel\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\{#MyAppExeName},0"
Root: HKCR; Subkey: "TableDeDaniel\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""%1"""   
