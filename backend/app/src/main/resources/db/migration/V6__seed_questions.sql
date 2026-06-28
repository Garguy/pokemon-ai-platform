INSERT INTO questions (text, trait_category, weight, display_order) VALUES
-- ENERGY (3 questions)
('You feel energised after spending time with a large group of people.', 'ENERGY', 1.0, 1),
('You prefer action over lengthy planning sessions.',                    'ENERGY', 1.2, 2),
('You often seek out new physical or social challenges.',                'ENERGY', 0.8, 3),

-- CURIOSITY (3 questions)
('You enjoy exploring topics outside your area of expertise.',          'CURIOSITY', 1.0, 4),
('You frequently ask "why" when you encounter something unfamiliar.',   'CURIOSITY', 1.2, 5),
('You find it easy to get lost in a book or a documentary.',            'CURIOSITY', 0.8, 6),

-- LEADERSHIP (3 questions)
('You naturally take charge when a group lacks direction.',             'LEADERSHIP', 1.0, 7),
('Others often turn to you for decisions under pressure.',              'LEADERSHIP', 1.2, 8),
('You feel comfortable speaking in front of a crowd.',                  'LEADERSHIP', 0.8, 9),

-- LOYALTY (3 questions)
('You prioritise the needs of close friends over personal convenience.','LOYALTY', 1.0, 10),
('You keep commitments even when it is inconvenient to do so.',         'LOYALTY', 1.2, 11),
('You feel a strong sense of duty to the groups you belong to.',        'LOYALTY', 0.8, 12),

-- RISK (3 questions)
('You would rather try something bold and fail than play it safe.',     'RISK', 1.0, 13),
('Uncertainty excites rather than unsettles you.',                      'RISK', 1.2, 14),
('You make important decisions quickly without overthinking.',          'RISK', 0.8, 15),

-- CREATIVITY (3 questions)
('You regularly come up with unconventional solutions to problems.',    'CREATIVITY', 1.0, 16),
('You enjoy creative hobbies such as drawing, writing or music.',       'CREATIVITY', 1.2, 17),
('You prefer open-ended tasks over ones with a single correct answer.', 'CREATIVITY', 0.8, 18);
