# PROJECT_OVERVIEW.md

## Project Name
Chores

## Vision
A family chore management app that uses AI-powered photo 
verification to confirm chores are actually completed, 
eliminating the need for parents to physically check. 
Built as a learning project in Java/Spring Boot, with a 
long-term roadmap to evolve into a full family home hub 
(calendar, shopping list, family messages, grounding 
tracker, etc.).

## The Problem
- 6 kids, constant chore arguments
- Parents can't always physically verify chores are done
- Kids claim chores are done when they aren't
- Hard to track who did what and when
- Existing apps don't do true AI photo verification 
  against a clean reference photo
- After-dinner kitchen chores affect next-day device 
  privileges — hard to remember without a tracking system

## The Solution
A dedicated locked-down Android device (chore station) 
shared by all kids. Kids select their avatar, complete 
their chores, and submit photos for AI verification. 
Parents get a PWA dashboard to review, approve/reject, 
and manage all chore settings.

## Kids (v1)
5 kids on the system (ages 16, 14, 12, 10, 7).
Placeholder names: Kid1, Kid2, Kid3, Kid4, Kid5.
6 month old excluded until v3.

## Chore Philosophy
- Chores must be done but don't have to stay done
- Photo taken same day counts regardless of who helped
- AI is the first line of verification, parent is the 
  override
- Streaks and consistency reports replace nagging
- App tracks it so parents don't have to remember

## Meal-Based Chore Blocks (Non-School Days / Summer v1)
Four distinct chore moments per day:

| Block | When | Stakes |
|---|---|---|
| Daily chores + breakfast leave no trace | Morning | All electronics (2pm soft gate, not enforced by app) |
| Lunch leave no trace | After lunch | Electronics check |
| Dinner kitchen chore | After dinner | Personal device next morning (parent flag) |
| Saturday full chore set | Saturday morning | Earlier electronics unlock as reward |

## Dinner Kitchen Roles (Fixed, 12-month rotation)
Each kid has a specific dinner role manually configured 
in the parent dashboard. Roles rotate annually (manually 
triggered). Summer/break variation allows 1-2 kids to 
swap roles (e.g. table wipe → counter wipe) when daily 
leave no trace reduces table cleaning need.

## Sunday
Kitchen chores only. Not enforced by the app. 
Honor system.

## Screen Time Enforcement
The app does NOT enforce screen time or device lockdown 
for kids' personal devices. It only tracks chore 
completion status and flags to parents. Physical device 
collection (locked office door) remains parent-managed.

## Long-term Vision
Chores evolves into a family home hub:
- Wall-mounted tablet as always-on family display
- Calendar, shopping list, family messages
- Grounding tracker with exact end dates
- School schedule integration
- Age-appropriate chore adjustments as kids grow