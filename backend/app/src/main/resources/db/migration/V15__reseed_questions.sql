-- Replace the question set with 20 Jung-typology-flavored statements,
-- mapped onto the existing 6 trait categories (Path A). Existing answers are
-- cleared so the new question ids are consistent; users simply re-take the quiz.
DELETE FROM user_answers;
DELETE FROM questions;

INSERT INTO questions (text, trait_category, weight, display_order) VALUES
-- ENERGY (extraversion / outward energy)
('You feel recharged after spending time around other people.',                 'ENERGY', 1.0, 1),
('In group conversations you tend to speak up early rather than hang back.',     'ENERGY', 1.0, 2),
('Meeting new people energises you more than it drains you.',                    'ENERGY', 1.0, 3),
('You are drawn to variety and new experiences over familiar routine.',          'ENERGY', 1.0, 4),

-- CURIOSITY (intuition / abstraction)
('When learning, you are drawn to big-picture theory before practical detail.',  'CURIOSITY', 1.0, 5),
('You naturally focus on patterns and future possibilities over concrete facts.','CURIOSITY', 1.0, 6),
('You enjoy exploring abstract ideas just to see where they lead.',              'CURIOSITY', 1.0, 7),

-- LEADERSHIP (assertiveness / decisiveness)
('You are comfortable making the call quickly when a group is stuck.',           'LEADERSHIP', 1.0, 8),
('You tend to be direct and to the point rather than diplomatic.',               'LEADERSHIP', 1.0, 9),
('You trust your first impression and act on it.',                               'LEADERSHIP', 1.0, 10),

-- LOYALTY (feeling / people-orientation)
('You weigh people''s feelings and the human impact heavily when deciding.',     'LOYALTY', 1.0, 11),
('You would rather collaborate as a team than work on your own.',                'LOYALTY', 1.0, 12),
('You keep your commitments even when it is inconvenient to do so.',             'LOYALTY', 1.0, 13),

-- RISK (perceiving / spontaneity)
('You would rather make a bold move and risk failing than play it safe.',        'RISK', 1.0, 14),
('You are comfortable starting important tasks close to the deadline.',          'RISK', 1.0, 15),
('Uncertainty excites you more than it unsettles you.',                          'RISK', 1.0, 16),
('You prefer flexible, open-ended plans over fixed schedules.',                  'RISK', 1.0, 17),

-- CREATIVITY (openness / improvisation)
('You prefer room to interpret your own approach over step-by-step instructions.','CREATIVITY', 1.0, 18),
('You like to question and adapt rules depending on the context.',               'CREATIVITY', 1.0, 19),
('You gravitate toward open-ended problems over ones with a single right answer.','CREATIVITY', 1.0, 20);
