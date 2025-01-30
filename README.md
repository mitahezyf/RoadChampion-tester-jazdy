Programowanie urządzeń mobilnych laboratorium L_X_ 

# Dokumentacja projetu: **Tester jazdy - licznyk gwałtownych przyśpieszeń**

## Zespoł projetowy:
Krzysztof Pomarański

## Opis projektu
Aplikacja ma za zadanie mierzyć ilość gwałtownych przyśpieszeń podczas poruszania się, dodatkowymi funkcjami są także obliczanie średniej prędkości z pokonanej trasy oraz możliwość podglądania jak dana trasa wyglądała na mapie po jej zakończeniu.


## Zakres projektu opis funkcjonalności:


## Panele / zakładki aplikacji 
- Panel logowania

![image](https://github.com/user-attachments/assets/a271c59d-e3f5-4c74-99b5-7300d4db32a4)

...

## Baza danych
<div align="center">
  <img src="Diagram ERD.PNG" align=center>
</div>

Baza danych składa się z 2 tabel. Pierwsza z nich przechowuje dane na temat czasu startu i końca danej trasy, średniej prędkości z trasy oraz ilości przyśpieszeń i hamowań, Natomiast druga tabela jest w relacji 1:N z zapisanymy koordynatami każdego zapisanego punktu lokalizacji wraz z sygnaturą czasową timestamp.

## Wykorzystane uprawnienia aplikacji do:
- Lokalizacji,
- Akcelerometru,
- Wibracji


Aplikacja nie potrzebuje żadnej wstępnej konfiguracji, jest w postaci "install&play"
