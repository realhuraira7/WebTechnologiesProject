mport 'dart:io';

void main() {
  stdout.write('Enter a number between 1 and 7: ');
  String? input = stdin.readLineSync();
  int? day = int.tryParse((input ?? '').trim());

  if (day == null) {
    print('Invalid day');
    return;
  }

  printDayName(day);
}

void printDayName(int day) {
  switch (day) {
    case 1:
      print('Monday');
      break;
    case 2:
      print('Tuesday');
      break;
    case 3:
      print('Wednesday');
      break;
    case 4:
      print('Thursday');
      break;
    case 5:
      print('Friday');
      break;
    case 6:
      print('Saturday');
      break;
    case 7:
      print('Sunday');
      break;
    default:
      print('Invalid day');
  }
}
