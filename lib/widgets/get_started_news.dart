import 'package:flutter/material.dart';

class GetStartedNews extends StatelessWidget {
  final Function navigateToWebsitesScreen;

  const GetStartedNews({
    Key? key,
    required this.navigateToWebsitesScreen,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 16),
        child: Column(
          children: [
            Container(
              padding: const EdgeInsets.all(50),
              child: Image.asset(
                'assets/follow-websites.png',
              ),
            ),
            const SizedBox(
              height: 16,
            ),
            Text(
              'Ακολούθησε websites',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.headline6,
            ),
            const SizedBox(
              height: 16,
            ),
            const Text(
              'Δημιούργησε το δικό σου personalized feed.',
              textAlign: TextAlign.center,
            ),
            const SizedBox(
              height: 16,
            ),
            ElevatedButton.icon(
              onPressed: () => navigateToWebsitesScreen(context),
              icon: const Icon(
                Icons.add,
              ),
              label: const Text(
                'Ακολούθησε',
                style: TextStyle(
                  fontSize: 16,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}