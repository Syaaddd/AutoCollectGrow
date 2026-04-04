# Slimefun4 Installation Instructions

## Download Slimefun4

1. Go to: https://github.com/SlimefunGuguProject/Slimefun4/releases
2. Download the latest release `.jar` file (e.g., `Slimefun4-2024.3.1.jar`)
3. Rename it to `Slimefun4.jar`
4. Place it in this `lib/` folder

## Alternative: Auto-Download Script

Run this PowerShell script to download automatically:

```powershell
# Download latest Slimefun4
$release = Invoke-RestMethod -Uri "https://api.github.com/repos/SlimefunGuguProject/Slimefun4/releases/latest"
$asset = $release.assets | Where-Object { $_.name -like "*.jar" } | Select-Object -First 1
Invoke-WebRequest -Uri $asset.browser_download_url -OutFile "lib/Slimefun4.jar"
Write-Host "Downloaded: $($asset.name)"
```

## After Download

Run Maven build again:
```cmd
mvn clean compile
```
