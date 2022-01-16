import 'dart:convert';

import 'package:flutter/material.dart';

import '../core/local/locals.dart';

class Article {
  late String id;
  late String link;
  late List<Attachment> attachments;
  late List<String> categories;
  late String content;
  late Map<String, dynamic> extras;
  late String pubDate;
  late String title;
  late String source;

  Article(this.id, this.link, this.attachments, this.categories,
      this.content, this.extras, this.pubDate, this.title, this.source);

  String? frontalImage;
  String? getFrontalImage() {
    if(frontalImage == null) {
      for (var element in attachments) {
        if(element.attribute == 'img') {
          frontalImage = element.value;
          break;
        }
      }
    }

    return frontalImage;
  }

  String getElapsedTime(context) {
    final articleDate = DateTime.parse(pubDate);
    final nowDate = DateTime.now();

    final diff = nowDate.difference(articleDate);

    if(diff.inDays > 365 + 365) {
      return "${diff.inDays ~/ 365} ${Locals.of(context)!.datetimeYears}";
    }
    else if(diff.inDays >= 365) {
      return "1 ${Locals.of(context)!.datetimeYear}";
    }
    else if(diff.inDays > 30 + 30) {
      return "${(diff.inDays ~/ 30)} ${Locals.of(context)!.datetimeMonths}";
    }
    else if(diff.inDays >= 30) {
      return "1 ${Locals.of(context)!.datetimeMonth}";
    }
    else if(diff.inDays > 7 + 7) {
      return "${(diff.inDays ~/ 7)} ${Locals.of(context)!.datetimeWeeks}";
    }
    else if(diff.inDays >= 7) {
      return "1 ${Locals.of(context)!.datetimeWeek}";
    }
    else if(diff.inDays == 1) {
      return "${diff.inDays} ${Locals.of(context)!.datetimeDay}";
    }
    else if(diff.inDays > 1) {
      return "${diff.inDays} ${Locals.of(context)!.datetimeDays}";
    }
    else if(diff.inHours == 1) {
      return "${diff.inHours} ${Locals.of(context)!.datetimeHour}";
    }
    else if(diff.inHours > 1) {
      return "${diff.inHours} ${Locals.of(context)!.datetimeHours}";
    }
    else if(diff.inMinutes == 1) {
      return "${diff.inMinutes} ${Locals.of(context)!.datetimeMinute}";
    }
    else if(diff.inMinutes > 1) {
      return "${diff.inMinutes} ${Locals.of(context)!.datetimeMinutes}";
    }
    else {
      return Locals.of(context)!.datetimeNow;
    }
  }

  static List<Article> parseFromRequest(String res) {
    List<dynamic> result = json.decode(res);

    List<Article> parsed = [];
    for(int i = 0; i < result.length; i++) {
      parsed.add(_parseOne(result[i]));
    }

    return parsed;
  }

  static Article _parseOne(Map<String, dynamic> res) {
    List<dynamic> attachments = res["attachments"] ?? [];
    List<dynamic> categories = res["categories"] ?? [];

    return Article(
      res["_id"],
      res["link"],
      attachments.map((e) => Attachment(e["text"], e['value'], e['attribute'])).toList(),
      categories.map((e) => e.toString()).toList(),
      res["content"] ?? "",
      res["extras"] ?? {},
      res["pubDate"],
      res["title"],
      res["source"],
    );
  }
}

class Attachment {
  String text, value, attribute;
  Attachment(this.text, this.value, this.attribute);

  IconData get icon {
    if(attribute == 'img') return Icons.image_outlined;
    else if(text.endsWith('.pdf')) return Icons.picture_as_pdf_outlined;

    return Icons.description_outlined;
  }
}