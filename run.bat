@echo off
title Bank of Turkey Banka Uygulamasi (Masaustu)
echo Uygulama baslatiliyor, lutfen bekleyin...
cd /d "%~dp0"

:: Sistemde mvn (Maven) komutu kurulu mu kontrol et
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    :: Eğer sistemde Maven kuruluysa global komutu kullan (GitHub uyumluluğu için)
    mvn exec:java
) else (
    :: Eğer kurulu değilse lokal klasördeki Maven'ı göreceli (relative) yol ile kullan
    ..\maven\apache-maven-3.9.6\bin\mvn.cmd exec:java
)

if %errorlevel% neq 0 (
    echo.
    echo [HATA] Uygulama baslatilamadi veya hata ile sonlandi.
    pause
)
