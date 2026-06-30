-- V018: Seed essential reference data
-- Purpose: Populate the roles, genres, publishers, library branches,
--          system configuration, and notification templates required
--          for the application to start up correctly.
--
-- Uses MERGE INTO ... KEY (...) for idempotent inserts so this script
-- is safe to re-run (H2 syntax; equivalent to INSERT IGNORE in MySQL).

-- ============================================================
-- ROLES
-- Guard against re-insertion; V001 seeds these but MERGE is idempotent.
-- ============================================================
MERGE INTO role (name, description)
KEY (name)
VALUES
    ('ROLE_ADMIN',  'System administrator with full access to all features'),
    ('ROLE_STAFF',  'Library staff member with operational access'),
    ('ROLE_MEMBER', 'Registered library member with borrowing privileges');

-- ============================================================
-- GENRES
-- Standard genre taxonomy used when cataloguing new acquisitions.
-- ============================================================
INSERT INTO genre (name, description) VALUES
    ('Fiction',          'Imaginative or invented stories, characters, and settings'),
    ('Non-Fiction',      'Factual accounts and real-world topics'),
    ('Science Fiction',  'Speculative fiction exploring science, technology, and future societies'),
    ('Mystery',          'Fiction featuring a puzzling crime or event that must be solved'),
    ('Biography',        'Written accounts of a person''s life'),
    ('History',          'Study and documentation of past events'),
    ('Children''s',      'Books written for children and young readers'),
    ('Technology',       'Books about computing, software, engineering, and digital topics'),
    ('Romance',          'Love stories and relationship-focused fiction'),
    ('Self-Help',        'Books aimed at personal improvement and development'),
    ('Fantasy',          'Fiction featuring magical worlds, mythical creatures, and epic quests'),
    ('Thriller',         'Fast-paced fiction with suspense and high stakes'),
    ('Horror',           'Fiction intended to frighten, disturb, or unsettle the reader'),
    ('Poetry',           'Literary work primarily focused on the use of imagery and rhythm'),
    ('Drama',            'Narrative works intended for theatrical performance');

-- ============================================================
-- PUBLISHERS
-- Major publishers pre-loaded to support catalog data entry.
-- ============================================================
INSERT INTO publisher (name, website, address, email) VALUES
    ('Penguin Random House',    'https://www.penguinrandomhouse.com',  '1745 Broadway, New York, NY 10019',              'info@penguinrandomhouse.com'),
    ('HarperCollins',           'https://www.harpercollins.com',       '195 Broadway, New York, NY 10007',               'info@harpercollins.com'),
    ('Simon & Schuster',        'https://www.simonandschuster.com',    '1230 Avenue of the Americas, New York, NY 10020','info@simonandschuster.com'),
    ('Hachette Book Group',     'https://www.hachettebookgroup.com',   '1290 Avenue of the Americas, New York, NY 10104','info@hachette.com'),
    ('Macmillan Publishers',    'https://www.macmillan.com',           '120 Broadway, New York, NY 10271',               'info@macmillan.com'),
    ('Oxford University Press', 'https://global.oup.com',              'Great Clarendon Street, Oxford, OX2 6DP, UK',   'info@oup.com'),
    ('Cambridge University Press', 'https://www.cambridge.org',        'Shaftesbury Road, Cambridge, CB2 8EA, UK',      'info@cambridge.org'),
    ('Scholastic',              'https://www.scholastic.com',          '557 Broadway, New York, NY 10012',               'info@scholastic.com');

-- ============================================================
-- LIBRARY BRANCHES
-- The three initial physical locations of City Public Library.
-- ============================================================
INSERT INTO library_branch (name, address, city, phone, email, opening_hours) VALUES
    ('Main Branch',
     '123 Main Street',  'Cityville', '(555) 100-0001', 'main@citylibrary.org',
     'Mon-Fri: 9am-8pm, Sat: 9am-5pm, Sun: 1pm-5pm'),
    ('North Branch',
     '456 Oak Avenue',   'Cityville', '(555) 100-0002', 'north@citylibrary.org',
     'Mon-Fri: 10am-7pm, Sat: 10am-4pm, Sun: Closed'),
    ('South Branch',
     '789 Pine Road',    'Cityville', '(555) 100-0003', 'south@citylibrary.org',
     'Mon-Wed: 10am-6pm, Thu-Fri: 12pm-8pm, Sat: 10am-3pm');

-- ============================================================
-- SYSTEM CONFIGURATION
-- Runtime-tunable parameters; application reads these at startup.
-- Fines, loan periods, and thresholds can all be adjusted here
-- without a code deployment.
-- ============================================================
INSERT INTO system_config (config_key, config_value, description, editable) VALUES
    ('max.loans.per.member',               '8',     'Maximum number of active loans allowed per member',                      TRUE),
    ('default.loan.period.days',           '21',    'Default loan period in days for standard books',                         TRUE),
    ('hold.expiry.days',                   '7',     'Number of days a hold is kept after becoming ready for pickup',          TRUE),
    ('overdue.grace.period.days',          '1',     'Grace period in days before overdue fines begin accruing',               TRUE),
    ('max.renewals',                       '3',     'Maximum number of times a loan can be renewed',                          TRUE),
    ('recommendation.cache.ttl.minutes',   '30',    'How long recommendation results are cached (in minutes)',                 TRUE),
    ('unpaid.fines.threshold',             '10.00', 'Maximum unpaid fines balance before checkout is blocked',                TRUE),
    ('member.cleanup.after.days',          '90',    'Days after deactivation before member data is anonymized',               TRUE),
    ('digital.loan.period.days',           '14',    'Default period for digital resource loans',                              TRUE),
    ('overdue.fine.rate.per.day',          '0.25',  'Standard daily fine rate for overdue physical items',                    TRUE),
    ('max.fine.per.item',                  '25.00', 'Maximum fine that can accrue on a single overdue item',                  TRUE),
    ('hold.queue.max.per.member',          '10',    'Maximum number of simultaneous holds a member may have',                 TRUE),
    ('search.results.page.size',           '20',    'Default number of results per search page',                              TRUE),
    ('app.version',                        '1.0.0', 'Current application version — managed by deployment pipeline',           FALSE),
    ('app.name',                           'City Public Library Catalog',
                                                    'Display name shown in email headers and UI page titles',                 FALSE);

-- ============================================================
-- NOTIFICATION TEMPLATES
-- Pre-built message templates for automated member communications.
-- Type values must match the CHECK constraint defined in V007.
-- ============================================================
INSERT INTO notification_template (name, type, subject_template, body_template, channel, active) VALUES

    ('hold-ready',
     'HOLD_READY',
     'Your Hold Is Ready for Pickup',
     'Dear {memberName}, your hold for "{bookTitle}" is ready for pickup at {branchName}. '
     || 'Please collect it by {expiryDate} or your hold will be released to the next patron in queue.',
     'EMAIL', TRUE),

    ('overdue-notice',
     'OVERDUE',
     'Overdue Library Item — Action Required',
     'Dear {memberName}, your item "{bookTitle}" was due on {dueDate} and is now {daysOverdue} day(s) overdue. '
     || 'A fine of ${fineAmount} has been applied to your account. '
     || 'Please return the item to any branch as soon as possible to prevent further charges.',
     'EMAIL', TRUE),

    ('welcome',
     'WELCOME',
     'Welcome to City Public Library!',
     'Dear {firstName}, welcome to City Public Library! '
     || 'Your membership number is {membershipNumber} and your membership is valid until {expiryDate}. '
     || 'You may borrow up to 8 items at a time. Visit us at any of our three branches or explore our online catalog.',
     'EMAIL', TRUE),

    ('fine-issued',
     'FINE_ISSUED',
     'Library Fine Notice — ${fineAmount} Applied',
     'Dear {memberName}, a fine of ${fineAmount} has been added to your account for: {reason}. '
     || 'Your total outstanding balance is now ${totalBalance}. '
     || 'Fines may be paid at any branch or through the member portal.',
     'IN_APP', TRUE),

    ('membership-expiring',
     'MEMBERSHIP_EXPIRING',
     'Your Library Membership Is Expiring Soon',
     'Dear {memberName}, your library membership expires on {expiryDate}. '
     || 'Please visit any branch or renew online to continue using library services without interruption.',
     'EMAIL', TRUE),

    ('due-soon',
     'DUE_SOON',
     'Library Item Due Soon — {bookTitle}',
     'Dear {memberName}, your item "{bookTitle}" is due on {dueDate}. '
     || 'Please return or renew it before the due date to avoid late fines. '
     || 'You may renew online, by phone, or at any branch.',
     'EMAIL', TRUE);
